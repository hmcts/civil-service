package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.variable.VariableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.exceptions.NotRetryableException;
import uk.gov.hmcts.reform.civil.helpers.ExternalTaskExceptionHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

import java.util.Objects;

/**
 * Interface for standard implementation of task handler that is invoked for each fetched and locked task.
 */
@SuppressWarnings("java:S6813")
public abstract class BaseExternalTaskHandler implements ExternalTaskHandler {

    public static final String FLOW_STATE = "flowState";
    public static final String FLOW_FLAGS = "flowFlags";

    protected final Logger log = LoggerFactory.getLogger(BaseExternalTaskHandler.class);

    private final ExternalTaskCompletionService externalTaskCompletionService;
    private final EventProperties eventProperties;

    protected BaseExternalTaskHandler(ExternalTaskCompletionService externalTaskCompletionService, EventProperties eventProperties) {
        this.externalTaskCompletionService = externalTaskCompletionService;
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

        try {
            log.info("External task '{}' started, processInstanceId '{}'", topicName, processInstanceId);
            ExternalTaskData externalTaskData = handleTask(externalTask);
            externalTaskCompletionService.completeTask(this, externalTask, externalTaskService, externalTaskData);
            log.debug("External task '{}' completed, processInstanceId '{}'", topicName, processInstanceId);
        } catch (BpmnError e) {
            log.error("External task '{}' BPMN error, processInstanceId '{}'", topicName, processInstanceId, e);
            externalTaskService.handleBpmnError(externalTask, e.getErrorCode());
        } catch (NotRetryableException e) {
            log.error("External task '{}' Not Recoverable error, processInstanceId '{}'", topicName, processInstanceId, e);
            handleTaskFailureNotRetryable(externalTask, externalTaskService, e);
        } catch (AssertionError | IllegalArgumentException e) {
            log.error("External task '{}' assertion / argument failure, processInstanceId '{}'", topicName, processInstanceId, e);
            handleTaskFailureNotRetryable(externalTask, externalTaskService, e);
        } catch (Exception e) {
            log.error("External task '{}' errored before handleFailure, processInstanceId '{}'", topicName, processInstanceId, e);
            if (ExternalTaskExceptionHelper.isNotRetryable(e)) {
                handleTaskFailureNotRetryable(externalTask, externalTaskService, e);
            } else {
                handleTaskFailure(externalTask, externalTaskService, e);
            }
        }
    }

    /**
     * Called when an exception arises and retry is not required from the {@link BaseExternalTaskHandler handleTask(externalTask)} method.
     *
     * @param externalTask        the external task to be handled.
     * @param externalTaskService to interact with fetched and locked tasks.
     * @param e                   the exception thrown by business logic.
     */
    public void handleTaskFailureNotRetryable(ExternalTask externalTask, ExternalTaskService externalTaskService, Throwable e) {
        log.info("Handle task failure Not Retryable, processInstanceId: '{}' ", externalTask.getProcessInstanceId());
        externalTaskService.handleFailure(
            externalTask,
            e.getMessage(),
            ExternalTaskExceptionHelper.getStackTrace(e, log),
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
    public void handleTaskFailure(ExternalTask externalTask, ExternalTaskService externalTaskService, Throwable e) {
        log.info("Handle task failure Retryable, processInstanceId: '{}' ", externalTask.getProcessInstanceId());
        int maxRetries = getMaxAttempts();
        Integer extRetries = externalTask.getRetries();
        int remainingRetries = extRetries != null ? extRetries : maxRetries;
        log.debug("External task retries: '{}', max: '{}' remaining: '{}'", extRetries, maxRetries, remainingRetries);

        externalTaskService.handleFailure(
            externalTask,
            e.getMessage(),
            ExternalTaskExceptionHelper.getStackTrace(e, log),
            remainingRetries - 1,
            calculateExponentialBackoff(maxRetries, remainingRetries)
        );
    }

    @SuppressWarnings("java:S1168")
    public VariableMap getVariableMap(ExternalTaskData data) {
        return null;
    }

    protected boolean isEventAlreadyProcessed(ExternalTask externalTask, BusinessProcess businessProcess) {
        return businessProcess != null
            && businessProcess.hasSameProcessInstanceId(externalTask.getProcessInstanceId())
            && Objects.equals(externalTask.getActivityId(), businessProcess.getActivityId());
    }

    protected int getMaxAttempts() {
        return eventProperties.getRetryCount();
    }

    protected abstract ExternalTaskData handleTask(ExternalTask externalTask);

    protected void throttle(long count) {
        long lock = eventProperties.getLockDuration();
        long delay = eventProperties.getDispatchDelay();
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
     * @param totalFound the total number of items found in the batch; if less than or equal to 25, no delay is applied.
     * @param lock       the duration for which the task is locked in milliseconds.
     * @param delay      the desired delay in milliseconds between task executions.
     * @return the calculated effective delay in milliseconds. Returns 0 if totalFound is less than or equal to 25.
     */
    private long calculateEffectiveDelay(long totalFound, long lock, long delay) {
        if (totalFound <= 25) {
            // skip for small batches
            return 0;
        }

        long maxExecutionTimeMs = (long) (lock * 0.8);
        long maxDelay = maxExecutionTimeMs / totalFound;
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
            long delay = eventProperties.getDispatchDelay();
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
