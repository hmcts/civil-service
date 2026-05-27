package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.exception.BadRequestException;
import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.exceptions.CompleteTaskException;
import uk.gov.hmcts.reform.civil.exceptions.NotRetryableException;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalTaskCompletionService {

    private static final Duration QA_FAILURE_DELAY = Duration.ofMinutes(30);
    private static final SecureRandom QA_FAILURE_RANDOM = new SecureRandom();

    @Value("${testing.support.enabled:false}")
    private boolean testingSupportEnabled;

    private final Instant startedAt = Instant.now();

    @Retryable(
        retryFor = CompleteTaskException.class,
        noRetryFor = NotRetryableException.class,
        notRecoverable = {NotRetryableException.class},
        maxAttemptsExpression = "${external-task-completion.retry.max-attempts:3}",
        backoff = @Backoff(
            delayExpression = "${external-task-completion.retry.delay:60000}",
            multiplierExpression = "${external-task-completion.retry.multiplier:15}"
        )
    )
    public void completeTask(BaseExternalTaskHandler handler,
                             ExternalTask externalTask,
                             ExternalTaskService externalTaskService,
                             ExternalTaskData data) throws CompleteTaskException, NotRetryableException {

        String topicName = externalTask.getTopicName();
        String processInstanceId = externalTask.getProcessInstanceId();
        log.info("Completing task '{}', processInstanceId '{}'", topicName, processInstanceId);

        try {
            maybeThrowQaCompletionException(topicName, processInstanceId);
            externalTaskService.complete(externalTask, handler.getVariableMap(data));
        } catch (NotFoundException e) {
            log.info(
                "Completing task '{}' NotFound error processInstanceId '{}' has already completed/aborted.",
                topicName, processInstanceId
            );
        } catch (BadRequestException e) {
            log.error("Completing task '{}' Bad Request error, processInstanceId '{}'", topicName, processInstanceId, e);
            throw new NotRetryableException(e.getMessage());
        } catch (ValueMapperException e) {
            log.error("Completing task '{}' Value Mapper error, processInstanceId '{}'", topicName, processInstanceId, e);
            throw new NotRetryableException(e.getMessage());
        } catch (NoSuchElementException e) {
            log.error("Completing task '{}' No Such Element error, processInstanceId '{}'", topicName, processInstanceId, e);
            throw new NotRetryableException(e.getMessage());
        } catch (Exception e) {
            // Inc EngineException | ConnectionLostException | UnknownHttpErrorException
            log.error("Completing task '{}' errored, processInstanceId '{}'", topicName, processInstanceId, e);
            throw new CompleteTaskException(e);
        }
    }

    @SuppressWarnings("java:S112")
    private void maybeThrowQaCompletionException(String topicName, String processInstanceId) throws Exception {
        if (!testingSupportEnabled || Instant.now().isBefore(startedAt.plus(QA_FAILURE_DELAY))) {
            return;
        }

        if (QA_FAILURE_RANDOM.nextInt(2) != 0) {
            return;
        }

        log.warn("Injecting QA completion failure for task '{}', processInstanceId '{}'", topicName, processInstanceId);

        switch (QA_FAILURE_RANDOM.nextInt(10)) {
            case 0:
                throw new NotFoundException("QA injected not found exception",
                    new BadRequestException("QA injected root cause", null));
            case 1:
                throw new BadRequestException("QA injected bad request exception", null);
            case 2:
                throw new ValueMapperException("QA injected value mapper exception");
            case 3:
                throw new NoSuchElementException("QA injected no such element exception");
            default:
                throw new Exception("QA injected retryable completion exception");
        }
    }

    @Recover
    public void recover(CompleteTaskException exception,
                        BaseExternalTaskHandler handler,
                        ExternalTask externalTask,
                        ExternalTaskService externalTaskService,
                        ExternalTaskData data) {
        log.error("Completing task '{}' All retry attempts failed, processInstanceId '{}', error message '{}'",
                  externalTask.getTopicName(), externalTask.getProcessInstanceId(), exception.getMessage()
        );
        handler.handleTaskFailureNotRetryable(externalTask, externalTaskService, exception);
    }
}
