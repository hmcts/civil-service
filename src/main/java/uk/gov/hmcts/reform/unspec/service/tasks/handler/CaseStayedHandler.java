package uk.gov.hmcts.reform.unspec.service.tasks.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.event.MoveCaseToStayedEvent;
import uk.gov.hmcts.reform.unspec.service.search.CaseStayedSearchService;

import java.util.List;

import static uk.gov.hmcts.reform.unspec.helpers.ExponentialRetryTimeoutHelper.calculateExponentialRetryTimeout;

@Slf4j
@RequiredArgsConstructor
@Component
public class CaseStayedHandler implements ExternalTaskHandler {

    private final CaseStayedSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        final String taskName = externalTask.getTopicName();

        try {
            List<CaseDetails> cases = caseSearchService.getCases();
            log.info("Job '{}' found {} case(s)", taskName, cases.size());

            cases.forEach(caseDetails -> applicationEventPublisher.publishEvent(
                new MoveCaseToStayedEvent(caseDetails.getId())));

            externalTaskService.complete(externalTask);
        } catch (Exception e) {
            int maxRetries = 3;
            int remainingRetries = externalTask.getRetries() == null ? maxRetries : externalTask.getRetries();

            externalTaskService.handleFailure(
                externalTask,
                externalTask.getWorkerId(),
                e.getMessage(),
                remainingRetries - 1,
                calculateExponentialRetryTimeout(500, maxRetries, remainingRetries)
            );

            log.error("Job '{}' errored due to {}", taskName, e.getMessage());
        }
    }
}
