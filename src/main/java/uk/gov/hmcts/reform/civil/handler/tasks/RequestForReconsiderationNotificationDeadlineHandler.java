package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.RequestForReconsiderationNotificationDeadlineEvent;
import uk.gov.hmcts.reform.civil.service.search.RequestForReconsiderationNotificationDeadlineSearchService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class RequestForReconsiderationNotificationDeadlineHandler implements BaseExternalTaskHandler {

    private final RequestForReconsiderationNotificationDeadlineSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void handleTask(ExternalTask externalTask) {
        List<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            applicationEventPublisher.publishEvent(new RequestForReconsiderationNotificationDeadlineEvent(caseDetails.getId()));
        });
    }
}
