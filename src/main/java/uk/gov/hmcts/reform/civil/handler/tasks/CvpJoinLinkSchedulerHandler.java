package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.CvpJoinLinkEvent;
import uk.gov.hmcts.reform.civil.service.search.CaseHearingDateSearchService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class CvpJoinLinkSchedulerHandler implements BaseExternalTaskHandler {

    private final CaseHearingDateSearchService searchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void handleTask(ExternalTask externalTask) {
        List<CaseDetails> cases = searchService.getCases();
        log.info("CVP Join Link Scheduler job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            try {
                applicationEventPublisher.publishEvent(new CvpJoinLinkEvent(caseDetails.getId()));
            } catch (Exception e) {
                log.error("Publishing 'CvpJoinLinkEvent' event for case id: '{}' failed", caseDetails.getId(), e);
            }
        });
    }
}
