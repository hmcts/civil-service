package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.RequestForReconsiderationNotificationDeadlineEvent;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.search.RequestForReconsiderationNotificationDeadlineSearchService;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class RequestForReconsiderationNotificationDeadlineHandler extends BaseExternalTaskHandler {

    private final RequestForReconsiderationNotificationDeadlineSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        Set<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            applicationEventPublisher.publishEvent(new RequestForReconsiderationNotificationDeadlineEvent(caseDetails.getId()));
        });
        return ExternalTaskData.builder().build();
    }
}
