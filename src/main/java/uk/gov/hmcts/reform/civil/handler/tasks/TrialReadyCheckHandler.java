package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.TrialReadyCheckEvent;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.search.TrialReadyCheckSearchService;

import java.util.Set;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

@Slf4j
@Component
public class TrialReadyCheckHandler extends BaseExternalTaskHandler {

    private final TrialReadyCheckSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TrialReadyCheckHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        TrialReadyCheckSearchService caseSearchService,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.caseSearchService = caseSearchService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        Set<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            try {
                applicationEventPublisher.publishEvent(new TrialReadyCheckEvent(caseDetails.getId()));
            } catch (Exception e) {
                log.error("Updating case with id: '{}' failed", caseDetails.getId(), e);
            }
        });
        return new ExternalTaskData();
    }

}
