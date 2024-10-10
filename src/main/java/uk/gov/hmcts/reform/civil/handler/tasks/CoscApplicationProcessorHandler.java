package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.CoscApplicationProcessorEvent;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.CoscApplicationSearchService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class CoscApplicationProcessorHandler implements BaseExternalTaskHandler {

    private final CoscApplicationSearchService coscApplicationSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final FeatureToggleService featureToggleService;

    @Override
    public void handleTask(ExternalTask externalTask) {
        if (!featureToggleService.isCoSCEnabled()) {
            return;
        }

        List<CaseDetails> cases = coscApplicationSearchService.getCases();
        log.info("COSC Application Processor Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            try {
                log.info("Processing case caseId '{}'", caseDetails.getId());
                applicationEventPublisher.publishEvent(new CoscApplicationProcessorEvent(caseDetails.getId()));
            } catch (Exception e) {
                log.error("COSC Application Processor Job failed to process case with id: '{}'", caseDetails.getId(), e);
            }
        });
    }
}
