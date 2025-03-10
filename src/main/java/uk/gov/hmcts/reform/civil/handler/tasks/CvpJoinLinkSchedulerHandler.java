package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.CvpJoinLinkEvent;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.search.CaseHearingDateSearchService;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class CvpJoinLinkSchedulerHandler extends BaseExternalTaskHandler {

    private final CaseHearingDateSearchService searchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        Set<CaseDetails> cases = searchService.getCases();
        log.info("CVP Join Link Scheduler job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            try {
                log.info("Publishing event for case id: '{}'", caseDetails.getId());
                applicationEventPublisher.publishEvent(new CvpJoinLinkEvent(caseDetails.getId()));
            } catch (Exception e) {
                log.error("Publishing 'CvpJoinLinkEvent' event for case id: '{}' failed", caseDetails.getId(), e);
            }
        });
        return ExternalTaskData.builder().build();
    }
}
