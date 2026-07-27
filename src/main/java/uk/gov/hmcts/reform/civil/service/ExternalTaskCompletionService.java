package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.exception.BadRequestException;
import org.camunda.bpm.client.exception.EngineException;
import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.exception.ValueMapperException;
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

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalTaskCompletionService {

    @Retryable(
        retryFor = CompleteTaskException.class,
        noRetryFor = NotRetryableException.class,
        notRecoverable = {NotRetryableException.class},
        maxAttemptsExpression = "${external-task-completion.retry.max-attempts:4}",
        backoff = @Backoff(
            delayExpression = "${external-task-completion.retry.delay:30000}",
            multiplierExpression = "${external-task-completion.retry.multiplier:5}"
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
            externalTaskService.complete(externalTask, handler.getVariableMap(data));
        } catch (NotFoundException e) {
            log.info(
                "Completing task '{}' NotFound error, processInstanceId '{}' has already completed/aborted.",
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
        } catch (EngineException e) {
            log.error("Completing task '{}' Camunda BPM client exception, processInstanceId '{}'", topicName, processInstanceId, e);
            throw new NotRetryableException(e.getMessage());
        } catch (Exception e) {
            // Inc EngineException | ConnectionLostException | UnknownHttpErrorException
            log.error("Completing task '{}' error, processInstanceId '{}'", topicName, processInstanceId, e);
            throw new CompleteTaskException(e);
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
