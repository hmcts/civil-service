package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardCallbackHandler;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT;

@Service
public class CCJRequestedDashboardNotificationHandler extends DashboardCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CREATE_DASHBOARD_NOTIFICATION_FOR_CCJ_REQUEST_FOR_APPLICANT1);
    public static final String TASK_ID = "GenerateDashboardNotificationClaimantIntentCCJRequestedForApplicant1";

    public CCJRequestedDashboardNotificationHandler(DashboardScenariosService dashboardScenariosService,
                                                    DashboardNotificationsParamsMapper mapper,
                                                    FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper, featureToggleService);
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public boolean shouldRecordScenario(CaseData caseData) {
        return YesOrNo.NO.equals(caseData.getApplicant1Represented());
    }

    @Override
    public String getScenario(CaseData caseData) {

        if (claimantSubmitsEitherCCJorDJWithoutSA(caseData)) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_CCJ_REQUESTED_CLAIMANT.getScenario();
        }
        return SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT.getScenario();
    }

    private boolean claimantSubmitsEitherCCJorDJWithoutSA(CaseData caseData) {
        LocalDate whenWillThisAmountBePaid =
            Optional.ofNullable(caseData.getRespondToClaimAdmitPartLRspec())
                .map(RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid).orElse(
                null);
        return (nonNull(whenWillThisAmountBePaid)
            && whenWillThisAmountBePaid.isBefore(LocalDate.now())
            && caseData.isFullAdmitPayImmediatelyClaimSpec())
            || ((!caseData.getDefaultJudgmentDocuments().isEmpty() && caseData.getDefaultJudgmentDocuments().stream()
            .map(el -> el.getValue())
            .anyMatch(doc -> doc.getDocumentType().equals(DocumentType.DEFAULT_JUDGMENT)))
            || (Objects.nonNull(caseData.getRepaymentSummaryObject())));
    }
}
