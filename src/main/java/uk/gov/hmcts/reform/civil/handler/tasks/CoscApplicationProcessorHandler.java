package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.event.CoscApplicationProcessorEvent;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.CoscApplicationSearchService;

import java.util.Set;

@Slf4j
@Component
public class CoscApplicationProcessorHandler extends BaseExternalTaskHandler {

    private static final String SCHEDULER_NAME = "CoscApplicationProcessor";

    private final CoscApplicationSearchService coscApplicationSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final FeatureToggleService featureToggleService;

    public CoscApplicationProcessorHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        CoscApplicationSearchService coscApplicationSearchService,
        ApplicationEventPublisher applicationEventPublisher,
        FeatureToggleService featureToggleService
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.coscApplicationSearchService = coscApplicationSearchService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.featureToggleService = featureToggleService;
    }

    public ExternalTaskData handleTask(ExternalTask externalTask) {
        if (featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)) {
            return new ExternalTaskData();
        }

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
        return new ExternalTaskData();
    }

}
