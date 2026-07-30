package uk.gov.hmcts.reform.civil.handler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.event.FullAdmitPayImmediatelyNoPaymentFromDefendantEvent;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.FullAdmitPayImmediatelyNoPaymentFromDefendantSearchService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DATA;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

@Slf4j
@Component
public class FullAdmitPayImmediatelyNoPaymentFromDefendantHandler extends BaseExternalTaskHandler {

    private final FullAdmitPayImmediatelyNoPaymentFromDefendantSearchService caseSearchService;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final CoreCaseDataService coreCaseDataService;

    private static final String SCHEDULER_NAME = "FullAdmitPayImmediatelyNoPaymentFromDefendant";
    private final FeatureToggleService featureToggleService;

    public FullAdmitPayImmediatelyNoPaymentFromDefendantHandler(
        ExternalTaskCompletionService externalTaskCompletionService,
        EventProperties eventProperties,
        FullAdmitPayImmediatelyNoPaymentFromDefendantSearchService caseSearchService,
        ApplicationEventPublisher applicationEventPublisher,
        CoreCaseDataService coreCaseDataService,
        FeatureToggleService featureToggleService
    ) {
        super(externalTaskCompletionService, eventProperties);
        this.caseSearchService = caseSearchService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.coreCaseDataService = coreCaseDataService;
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
            Long caseId = caseDetails.getId();
            try {
                setFullAdmitNoPaymentSchedulerProcessed(caseId);
                applicationEventPublisher.publishEvent(new FullAdmitPayImmediatelyNoPaymentFromDefendantEvent(
                    caseId));
            } catch (Exception e) {
                log.error("Updating case with id: '{}' failed", caseId, e);
            }
        });
        return new ExternalTaskData();
    }

    protected void setFullAdmitNoPaymentSchedulerProcessed(Long caseId) {
        String eventSummary = "Updating case - Full Admit No Payment Dashboard notification created successfully";
        String eventDescription = "Updating case - Full Admit No Payment Dashboard notification created successfully";

        Map<String, Object> newCaseData = new HashMap<>();
        newCaseData.put("fullAdmitNoPaymentSchedulerProcessed", YesOrNo.YES);

        coreCaseDataService.triggerEvent(
            caseId,
            UPDATE_CASE_DATA,
            newCaseData,
            eventSummary,
            eventDescription
        );
    }

}
