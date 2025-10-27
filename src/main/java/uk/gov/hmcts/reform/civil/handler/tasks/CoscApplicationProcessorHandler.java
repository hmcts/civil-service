package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.CoscApplicationProcessorEvent;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.search.CoscApplicationSearchService;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class CoscApplicationProcessorHandler extends BaseExternalTaskHandler {

    private final CoscApplicationSearchService coscApplicationSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ExternalTaskData handleTask(ExternalTask externalTask) {
        Set<CaseDetails> cases = coscApplicationSearchService.getCases();
        log.info("COSC Application Processor Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            try {
                log.info("Processing case caseId '{}'", caseDetails.getId());
                applicationEventPublisher.publishEvent(new CoscApplicationProcessorEvent(caseDetails.getId()));
            } catch (Exception e) {
                log.error("COSC Application Processor Job failed to process case with id: '{}'", caseDetails.getId(), e);
            }
        });
        return ExternalTaskData.builder().build();
    }
}
