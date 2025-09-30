package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS;

public abstract class DeleteWrittenRepresentationNotificationHandler extends DashboardCallbackHandler {

    public DeleteWrittenRepresentationNotificationHandler(DashboardScenariosService dashboardScenariosService,
                                                          DashboardNotificationsParamsMapper mapper,
                                                          FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper, featureToggleService);
    }

    protected boolean shouldTriggerApplicantNotification(CaseData caseData) {
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
