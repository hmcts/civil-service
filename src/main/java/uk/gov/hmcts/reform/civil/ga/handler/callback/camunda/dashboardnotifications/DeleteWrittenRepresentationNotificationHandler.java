package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications;

import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.callback.GaDashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS;

public abstract class DeleteWrittenRepresentationNotificationHandler extends GaDashboardCallbackHandler {

    public DeleteWrittenRepresentationNotificationHandler(DashboardApiClient dashboardApiClient,
                                                          GaDashboardNotificationsParamsMapper mapper,
                                                          FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
    }

    protected boolean shouldTriggerApplicantNotification(GeneralApplicationCaseData caseData) {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDate applicantDeadlineDate = caseData.getParentClaimantIsApplicant() == YesOrNo.YES
            ? caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getSequentialApplicantMustRespondWithin()
            : caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getWrittenSequentailRepresentationsBy();
        if (caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getWrittenOption() != SEQUENTIAL_REPRESENTATIONS
            || currentTime.isAfter(applicantDeadlineDate.atTime(DeadlinesCalculator.END_OF_BUSINESS_DAY))) {
            return false;
        }
        LocalDate respondentDeadlineDate = caseData.getParentClaimantIsApplicant() == YesOrNo.NO
            ? caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getSequentialApplicantMustRespondWithin()
            : caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getWrittenSequentailRepresentationsBy();
        LocalDateTime respondentDeadline = respondentDeadlineDate.atTime(DeadlinesCalculator.END_OF_BUSINESS_DAY);
        return currentTime.isAfter(respondentDeadline) && respondentDeadline.plusDays(1).isAfter(currentTime);
    }
}
