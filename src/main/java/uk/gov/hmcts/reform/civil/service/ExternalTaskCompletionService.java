package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.exception.BadRequestException;
import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.exceptions.CompleteTaskException;
import uk.gov.hmcts.reform.civil.exceptions.NotRetryableException;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalTaskCompletionService {

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
        log.info("Trying to complete external task '{}' finished with processInstanceId '{}'",
                 topicName, processInstanceId);

        try {
            externalTaskService.complete(externalTask, handler.getVariableMap(data));
            log.info("External task '{}' completed with processInstanceId '{}'", topicName, processInstanceId);
        } catch (NotFoundException e) {
            log.info(
                "Completing external task '{}' was skipped as process instance '{}' has already completed.",
                topicName, processInstanceId
            );
        } catch (BadRequestException e) {
            log.error(
                "Completing external task '{}' errored with processInstanceId '{}', bad request error",
                topicName, processInstanceId
            );
            throw new NotRetryableException(e.getMessage());
        } catch (Exception e) {
            log.error("Completing external task '{}' errored  with processInstanceId '{}'", topicName, processInstanceId, e);
            throw new CompleteTaskException(e);
        }
    }

    @Recover
    public void recover(CompleteTaskException exception,
                        BaseExternalTaskHandler handler,
                        ExternalTask externalTask,
                        ExternalTaskService externalTaskService,
                        ExternalTaskData data) {
        log.error("All attempts to completing task '{}' failed  with processInstanceId '{}' with error message '{}'",
                  externalTask.getTopicName(), externalTask.getProcessInstanceId(), exception.getMessage()
        );
        handler.handleFailureNotRetryable(externalTask, externalTaskService, exception);
    }
}
