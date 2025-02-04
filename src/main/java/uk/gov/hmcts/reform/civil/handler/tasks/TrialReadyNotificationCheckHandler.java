package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.TrialReadyNotificationEvent;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.search.TrialReadyNotificationSearchService;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class TrialReadyNotificationCheckHandler extends BaseExternalTaskHandler {

    private final TrialReadyNotificationSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        Set<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            try {
                applicationEventPublisher.publishEvent(new TrialReadyNotificationEvent(caseDetails.getId()));
            } catch (Exception e) {
                log.error("Updating case with id: '{}' failed", caseDetails.getId(), e);
            }
        });
        return ExternalTaskData.builder().build();
    }
}
