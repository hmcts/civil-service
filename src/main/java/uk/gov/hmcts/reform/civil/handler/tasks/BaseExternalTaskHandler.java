package uk.gov.hmcts.reform.civil.handler.tasks;

import feign.FeignException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.variable.VariableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import uk.gov.hmcts.reform.civil.exceptions.CompleteTaskException;
import uk.gov.hmcts.reform.civil.exceptions.NotRetryableException;

import java.util.Arrays;

import static uk.gov.hmcts.reform.civil.helpers.ExponentialRetryTimeoutHelper.calculateExponentialRetryTimeout;

/**
 * Interface for standard implementation of task handler that is invoked for each fetched and locked task.
 */
public interface BaseExternalTaskHandler extends ExternalTaskHandler {

    String FLOW_STATE = "flowState";
    String FLOW_FLAGS = "flowFlags";

    Logger log = LoggerFactory.getLogger(BaseExternalTaskHandler.class);

    /**
     * Executed for each fetched and locked task.
     *
     * @param externalTask        the context is represented of.
     * @param externalTaskService to interact with fetched and locked tasks.
     */
    @Override
    default void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String topicName = externalTask.getTopicName();
        String processInstanceId = externalTask.getProcessInstanceId();
        boolean handleTaskSucceeded = false;

        try {
            log.info("External task '{}' started with processInstanceId '{}'",
                     topicName, processInstanceId
            );
            handleTask(externalTask);
            handleTaskSucceeded = true;
        } catch (BpmnError e) {
            log.error("Bpmn error for external task '{}' with processInstanceId '{}'",
                      topicName, processInstanceId, e
            );
            externalTaskService.handleBpmnError(externalTask, e.getErrorCode());
        } catch (NotRetryableException e) {
            log.error("External task '{}' errored  with processInstanceId '{}'",
                      topicName, processInstanceId, e
            );
            handleFailureNotRetryable(externalTask, externalTaskService, e);
        } catch (Exception e) {
            log.error("External task before handleFailure '{}' errored  with processInstanceId '{}'",
                      topicName, processInstanceId, e
            );
            handleFailure(externalTask, externalTaskService, e);
        }

        if (handleTaskSucceeded) {
            completeTask(externalTask, externalTaskService);
        }
    }

    //Total possible waiting time 16 minutes - if changing this, change lockDuration in ExternalTaskListenerConfiguration
    @Retryable(value = CompleteTaskException.class, maxAttempts = 3, backoff = @Backoff(delay = 60000, multiplier = 15))
    default void completeTask(ExternalTask externalTask, ExternalTaskService externalTaskService) throws CompleteTaskException {
        String topicName = externalTask.getTopicName();
        String processInstanceId = externalTask.getProcessInstanceId();

        try {
            externalTaskService.complete(externalTask, getVariableMap());
            log.info("External task '{}' finished with processInstanceId '{}'",
                     topicName, processInstanceId
            );
        } catch (Exception e) {
            log.error("Completing external task '{}' errored  with processInstanceId '{}'",
                      topicName, processInstanceId, e
            );
            throw new CompleteTaskException(e);
        }
    }

    @Recover
    default void recover(CompleteTaskException exception, ExternalTask externalTask, ExternalTaskService externalTaskService) {
        log.error("All attempts to completing task '{}' failed  with processInstanceId '{}' with error message '{}'",
                  externalTask.getTopicName(), externalTask.getProcessInstanceId(), exception.getMessage()
        );
        handleFailureNotRetryable(externalTask, externalTaskService, exception);
    }

    /**
     * Called when an exception arises from the {@link BaseExternalTaskHandler handleTask(externalTask)} method.
     *
     * @param externalTask        the external task to be handled.
     * @param externalTaskService to interact with fetched and locked tasks.
     * @param e                   the exception thrown by business logic.
     */
    default void handleFailure(ExternalTask externalTask, ExternalTaskService externalTaskService, Exception e) {
        int maxRetries = getMaxAttempts();
        int remainingRetries = externalTask.getRetries() == null ? maxRetries : externalTask.getRetries();
        log.info(
            "Handle failure externalTask.getRetries() is null ?? '{}' processInstanceId: '{}' " +
                "remainingRetries value : '{}' externalTask.getRetries() value: '{}' maxRetries: '{}'",
            externalTask.getRetries() == null,
            externalTask.getProcessInstanceId() != null ? externalTask.getProcessInstanceId() : "Instance id is null",
            remainingRetries,
            externalTask.getRetries(),
            maxRetries
        );

        externalTaskService.handleFailure(
            externalTask,
            e.getMessage(),
            getStackTrace(e),
            remainingRetries - 1,
            //Total possible waiting time 15 minutes - if changing this, change lockDuration in ExternalTaskListenerConfiguration
            calculateExponentialRetryTimeout(5 * 60 * 1000, maxRetries, remainingRetries)
        );
    }

    /**
     * Called when an exception arises and retry is not required from the {@link BaseExternalTaskHandler handleTask(externalTask)} method.
     *
     * @param externalTask        the external task to be handled.
     * @param externalTaskService to interact with fetched and locked tasks.
     * @param e                   the exception thrown by business logic.
     */
    default void handleFailureNotRetryable(ExternalTask externalTask, ExternalTaskService externalTaskService, Exception e) {
        int remainingRetries = 0;
        log.info(
            "No Retryable Handle failure processInstanceId: '{}' ",
            externalTask.getProcessInstanceId() != null ? externalTask.getProcessInstanceId() : "Instance id is null"
        );

        externalTaskService.handleFailure(
            externalTask,
            e.getMessage(),
            getStackTrace(e),
            remainingRetries,
            1000L
        );
    }

    private String getStackTrace(Throwable throwable) {
        if (throwable instanceof FeignException feignexception) {
            return feignexception.contentUTF8();
        }

        return Arrays.toString(throwable.getStackTrace());
    }

    /**
     * Defines the number of attempts for a given external task.
     *
     * @return the number of attempts for an external task.
     */
    default int getMaxAttempts() {
        return 3;
    }

    /**
     * Defines a Map of variables to be added to an external task on completion.
     * By default this is null, override to add values.
     *
     * @return the variables to add to the external task.
     */
    default VariableMap getVariableMap() {
        return null;
    }

    /**
     * Executed for each fetched and locked task.
     *
     * @param externalTask the external task to be handled.
     */
    void handleTask(ExternalTask externalTask);
}
