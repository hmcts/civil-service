package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.TrialReadyEvent;
import uk.gov.hmcts.reform.civil.service.search.TrialReadySearchService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class TrialReadyCheckHandler implements BaseExternalTaskHandler {

    private final TrialReadySearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void handleTask(ExternalTask externalTask) {
        List<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            try {
                applicationEventPublisher.publishEvent(new TrialReadyEvent(caseDetails.getId()));
            } catch (Exception e) {
                log.error("Updating case with id: '{}' failed", caseDetails.getId(), e);
            }
        });
    }
}
