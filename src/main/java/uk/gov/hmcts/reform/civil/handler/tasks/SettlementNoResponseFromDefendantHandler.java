package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.event.SettlementNoResponseFromDefendantEvent;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.SettlementNoResponseFromDefendantSearchService;

import java.util.Set;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

@Slf4j
@Component
public class SettlementNoResponseFromDefendantHandler extends BaseExternalTaskHandler {

    private final SettlementNoResponseFromDefendantSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final String SCHEDULER_NAME = "SettlementNoResponseFromDefendantCheck";
    private final FeatureToggleService featureToggleService;

    public SettlementNoResponseFromDefendantHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        SettlementNoResponseFromDefendantSearchService caseSearchService,
        ApplicationEventPublisher applicationEventPublisher,
        FeatureToggleService featureToggleService
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.caseSearchService = caseSearchService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {

        if (featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)) {
            return new ExternalTaskData();
        }

        Set<CaseDetails> cases = caseSearchService.getCases();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(caseDetails -> {
            try {
                applicationEventPublisher.publishEvent(new SettlementNoResponseFromDefendantEvent(caseDetails.getId()));
            } catch (Exception e) {
                log.error("Updating case with id: '{}' failed", caseDetails.getId(), e);
            }
        });
        return new ExternalTaskData();
    }

}
