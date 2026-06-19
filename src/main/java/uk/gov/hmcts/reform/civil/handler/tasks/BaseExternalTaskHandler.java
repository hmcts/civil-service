package uk.gov.hmcts.reform.civil.handler.tasks;

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
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.exceptions.CompleteTaskException;
import uk.gov.hmcts.reform.civil.exceptions.NotRetryableException;
import uk.gov.hmcts.reform.civil.helpers.ExternalTaskExceptionHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;

import java.util.Objects;

/**
 * Interface for standard implementation of task handler that is invoked for each fetched and locked task.
 */
@SuppressWarnings({"java:S6813", "java:S1874"})
public abstract class BaseExternalTaskHandler implements ExternalTaskHandler {

    public static final String FLOW_STATE = "flowState";
    public static final String FLOW_FLAGS = "flowFlags";
    public static final int SMALL_BATCH = 25;

    protected final Logger log = LoggerFactory.getLogger(BaseExternalTaskHandler.class);

    private final EventProperties eventProperties;

    protected BaseExternalTaskHandler(EventProperties eventProperties) {
        this.eventProperties = eventProperties;
    }

    /**
     * Executed for each fetched and locked task.
     *
     * @param externalTask        the context is represented of.
     * @param externalTaskService to interact with fetched and locked tasks.
     */
    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String topicName = externalTask.getTopicName();
        String processInstanceId = externalTask.getProcessInstanceId();
        boolean handleTaskSucceeded = false;
        ExternalTaskData externalTaskData = null;

        try {
            log.info("External task '{}' started with processInstanceId '{}'",
                     topicName, processInstanceId
            );
            externalTaskData = handleTask(externalTask);
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
            completeTask(externalTask, externalTaskService, externalTaskData);
        }
    }

    @Retryable(value = CompleteTaskException.class, backoff = @Backoff(delay = 60000, multiplier = 15))
    protected void completeTask(ExternalTask externalTask, ExternalTaskService externalTaskService, ExternalTaskData data) throws CompleteTaskException {
        String topicName = externalTask.getTopicName();
        String processInstanceId = externalTask.getProcessInstanceId();
        log.info("Trying to complete external task '{}' finished with processInstanceId '{}'",
                 topicName, processInstanceId
        );

        try {
            externalTaskService.complete(externalTask, getVariableMap(data));
            log.info("External task '{}' completed with processInstanceId '{}'",
                     topicName, processInstanceId
            );
        } catch (Exception e) {
            log.error("Completing external task '{}' errored  with processInstanceId '{}'",
                      topicName, processInstanceId, e
            );
            throw new CompleteTaskException(e);
        }
    }

    protected boolean isEventAlreadyProcessed(ExternalTask externalTask, BusinessProcess businessProcess) {
        return businessProcess != null
            && businessProcess.hasSameProcessInstanceId(externalTask.getProcessInstanceId())
            && Objects.equals(externalTask.getActivityId(), businessProcess.getActivityId());
    }

    @Recover
    void recover(CompleteTaskException exception, ExternalTask externalTask, ExternalTaskService externalTaskService) {
        log.error("All attempts to completing task '{}' failed  with processInstanceId '{}' with error message '{}'",
                  externalTask.getTopicName(), externalTask.getProcessInstanceId(), exception.getMessage()
        );
        handleFailureNotRetryable(externalTask, externalTaskService, exception);
    }

    /**
     * Called when an exception arises and retry is not required from the {@link BaseExternalTaskHandler handleTask(externalTask)} method.
     *
     * @param externalTask        the external task to be handled.
     * @param externalTaskService to interact with fetched and locked tasks.
     * @param e                   the exception thrown by business logic.
     */
    void handleFailureNotRetryable(ExternalTask externalTask, ExternalTaskService externalTaskService, Throwable e) {
        log.info("Handle task failure Not Retryable, processInstanceId: '{}' ", externalTask.getProcessInstanceId());
        externalTaskService.handleFailure(
            externalTask,
            e.getMessage(),
            ExternalTaskExceptionHelper.getStackTrace(e),
            0,
            1000L
        );
    }

    /**
     * Called when an exception arises from the {@link BaseExternalTaskHandler handleTask(externalTask)} method.
     *
     * @param externalTask        the external task to be handled.
     * @param externalTaskService to interact with fetched and locked tasks.
     * @param e                   the exception thrown by business logic.
     */
    void handleFailure(ExternalTask externalTask, ExternalTaskService externalTaskService, Throwable e) {
        log.info("Handle task failure Retryable, processInstanceId: '{}' ", externalTask.getProcessInstanceId());
        int maxRetries = getMaxAttempts();
        Integer extRetries = externalTask.getRetries();
        int remainingRetries = extRetries != null ? extRetries : maxRetries;
        log.debug("External task retries: '{}', max: '{}' remaining: '{}'", extRetries, maxRetries, remainingRetries);

        externalTaskService.handleFailure(
            externalTask,
            e.getMessage(),
            ExternalTaskExceptionHelper.getStackTrace(e),
            remainingRetries - 1,
            calculateExponentialBackoff(maxRetries, remainingRetries)
        );
    }

    @SuppressWarnings("java:S1168")
    public VariableMap getVariableMap(ExternalTaskData data) {
        return null;
    }

    protected int getMaxAttempts() {
        return eventProperties.getRetryCount();
    }

    /**
     * Executed for each fetched and locked task.
     *
     * @param externalTask the external task to be handled.
     */
    protected abstract ExternalTaskData handleTask(ExternalTask externalTask);

    protected void throttle(long count) {
        long delay = eventProperties.getDispatchDelay();
        throttle(count, delay);
    }

    protected void throttle(long count, long delay) {
        long lock = eventProperties.getLockDuration();
        throttle(count, delay, lock);
    }

    protected void throttle(long count, long delay, long lock) {
        long effectiveDelay = calculateEffectiveDelay(count, lock, delay);
        if (effectiveDelay == 0) {
            return;
        }
        try {
            Thread.sleep(effectiveDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Calculates the effective delay for a batch processing task based on the total found items,
     * the lock duration, and the desired delay. Ensures the delay does not surpass the maximum
     * permissible delay derived from the lock duration and batch size.
     *
     * @param count the total number of items found in the batch; if less than or equal to 25, no delay is applied.
     * @param lock  the duration for which the task is locked in milliseconds.
     * @param delay the desired delay in milliseconds between task executions.
     * @return the calculated effective delay in milliseconds. Returns 0 if count is less than or equal to 25.
     */
    private long calculateEffectiveDelay(long count, long lock, long delay) {
        if (count <= 1 || delay <= 0 || lock <= 0) {
            // skip no-op or invalid delays
            return 0;
        }

        if (count <= SMALL_BATCH && delay < 2000L) {
            // skip for small & fast batches
            return 0;
        }

        long maxExecutionTimeMs = (long) (lock * 0.8);
        long maxDelay = maxExecutionTimeMs / count;
        return Math.min(maxDelay, delay);
    }

    /**
     * Calculates the current retry timeout value based on the remaining number of retries.
     *
     * @param maxRetries     the total number of times to retry. For a default Camunda external task this is 3.
     * @param remainingRetries the number of remaining retries. This will be the retries associated with the
     *                         external task {@link  org.camunda.bpm.client.task.ExternalTask}
     * @return a long value for retry timeout.
     */
    private long calculateExponentialBackoff(int maxRetries, int remainingRetries) {
        if (remainingRetries > 0 && remainingRetries <= maxRetries) {
            long lock = eventProperties.getLockDuration();
            long delay = eventProperties.getBackoffDelay();
            long maxBackoff = calculateEffectiveBackoff(maxRetries, lock, delay);
            double retryExponent = (double) maxRetries - remainingRetries;
            double multiplier = Math.pow(2D, retryExponent);
            return Math.round(maxBackoff * multiplier);
        }
        return 0L;
    }

    /**
     * Calculates the effective backoff delay based on the maximum number of retries, lock duration,
     * and specified delay. Ensures the delay does not exceed the maximum permissible delay
     * as determined by the lock duration and retry count.
     *
     * @param maxRetries the maximum number of retry attempts allowed; must be greater than 0.
     * @param lock       the duration for which the task is locked in milliseconds.
     * @param delay      the desired delay in milliseconds between retries.
     * @return the calculated effective backoff delay in milliseconds. Returns 0 if maxRetries is less than or equal to 0.
     */
    private long calculateEffectiveBackoff(int maxRetries, long lock, long delay) {
        if (maxRetries <= 0) {
            return 0L;
        }
        // Total possible waiting time 33 minutes max (retry 1 + 2 + 3) as of 2026
        long maxExecutionTimeMs = (long) (lock * 0.8);
        long denominator = Math.round(Math.pow(2D, maxRetries) - 1D);
        long maxDelay = maxExecutionTimeMs / denominator;
        return Math.min(maxDelay, delay);
    }

}
