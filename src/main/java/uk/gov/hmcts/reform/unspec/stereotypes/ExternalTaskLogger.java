package uk.gov.hmcts.reform.unspec.stereotypes;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;

import static uk.gov.hmcts.reform.unspec.helpers.ExponentialRetryTimeoutHelper.calculateExponentialRetryTimeout;

@Aspect
@Slf4j
public class ExternalTaskLogger {

    @Before(value = "execution(* org.camunda.bpm.client.task.ExternalTaskHandler.execute(..)) && args(externalTask,..)",
        argNames = "externalTask")
    public void logExternalTaskAtStart(ExternalTask externalTask) {
        final String taskName = externalTask.getTopicName();
        log.info("Job {} started", taskName);
    }

    @After(value = "execution(* org.camunda.bpm.client.task.ExternalTaskHandler.execute(..)) && args(externalTask,..)",
        argNames = "externalTask")
    public void logExternalTaskAtEnd(ExternalTask externalTask) {
        final String taskName = externalTask.getTopicName();
        log.info("Job '{}' finished", taskName);
    }

    @AfterThrowing(value = "execution(* org.camunda.bpm.client.task.ExternalTaskHandler.execute(..))  "
        + "&& args(externalTask,externalTaskService)",
        argNames = "externalTask, externalTaskService, throwable",
        throwing = "throwable")
    public void logExternalTaskAfterError(
        ExternalTask externalTask,
        ExternalTaskService externalTaskService,
        Throwable throwable
    ) {
        final String taskName = externalTask.getTopicName();
        int maxRetries = 3;
        int remainingRetries = externalTask.getRetries() == null ? maxRetries : externalTask.getRetries();
        externalTaskService.handleFailure(
            externalTask,
            externalTask.getWorkerId(),
            "Event failed processing",
            remainingRetries - 1,
            calculateExponentialRetryTimeout(500, maxRetries, remainingRetries)
        );
        log.error("Job '{}' errored due to {}", taskName, throwable.getMessage(), throwable);
    }
}
