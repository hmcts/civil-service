package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.DismissClaimEvent;
import uk.gov.hmcts.reform.civil.service.search.CaseDismissedSearchService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClaimDismissedHandler implements BaseExternalTaskHandler {

    private final CaseDismissedSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void handleTask(ExternalTask externalTask) {
        List<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        List<String> exceptions = new ArrayList<String>();

            cases.forEach(caseDetails -> {
                try {
                    applicationEventPublisher.publishEvent(new DismissClaimEvent(caseDetails.getId()));
                } catch (Exception e) {
                    exceptions.add("Updating case with id: '" + caseDetails.getId() + "' failed " + e);
                }
            });

        if(!exceptions.isEmpty()){
            throw new RuntimeException( "The following errors occurred: " + exceptions);
        }

    }
}
