package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.DismissClaimEvent;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.search.CaseDismissedSearchService;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClaimDismissedHandler extends BaseExternalTaskHandler {

    private final CaseDismissedSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        Set<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            try {
                applicationEventPublisher.publishEvent(new DismissClaimEvent(caseDetails.getId()));
            } catch (Exception e) {
                //Continue for other cases if there is some error in some cases, as we don't want
                // to stop processing other valid cases because error happened in some.
                //We log the error to leave a trace that something needs to be looked into for failed cases
                log.error("Updating case with id: '{}' failed", caseDetails.getId(), e);
            }
        });
        return ExternalTaskData.builder().build();
    }
}
