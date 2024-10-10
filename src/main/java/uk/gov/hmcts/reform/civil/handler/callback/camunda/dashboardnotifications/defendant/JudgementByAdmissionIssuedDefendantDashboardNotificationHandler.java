package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.callback.DashboardJudgementOnlineCallbackHandler;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGEMENTS_ONLINE_ISSUED_CCJ_DEFENDANT;

@Service
public class JudgementByAdmissionIssuedDefendantDashboardNotificationHandler extends DashboardJudgementOnlineCallbackHandler {

    private static final List<CaseEvent> EVENTS =
        List.of(CREATE_DASHBOARD_NOTIFICATION_JUDGEMENT_BY_ADMISSION_DEFENDANT);
    public static final String TASK_ID = "GenerateDashboardNotificationJudgementByAdmissionDefendant";

    public JudgementByAdmissionIssuedDefendantDashboardNotificationHandler(DashboardApiClient dashboardApiClient,
                                                                           DashboardNotificationsParamsMapper mapper,
                                                                           FeatureToggleService featureToggleService) {
        super(dashboardApiClient, mapper, featureToggleService);
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
    public String getScenario(CaseData caseData) {
        if (caseData.isRespondent1LiP()) {
            if (isJudgmentOrderIssued(caseData)) {
                return
                    SCENARIO_AAA6_JUDGEMENTS_ONLINE_ISSUED_CCJ_DEFENDANT.getScenario();
            } else if (
                caseData.hasApplicantAcceptedRepaymentPlan() && (caseData.isPayByInstallment() || caseData.isPayBySetDate())) {
                return
                    SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT.getScenario();
            }
        }
        return null;
    }

    private boolean isJudgmentOrderIssued(CaseData caseData) {
        if (caseData.isLRvLipOneVOne() && null != caseData.getDefenceAdmitPartPaymentTimeRouteRequired()) {
            return true;
        }
        return caseData.isLipvLipOneVOne()
            && (isIndividualOrSoleTraderWithJoIssued(caseData)
            || isCompanyOrOrganisationWithRepaymentPlanAccepted(caseData));
    }

    private boolean isIndividualOrSoleTraderWithJoIssued(CaseData caseData) {
        return caseData.getRespondent1().isIndividualORSoleTrader()
            && (caseData.getActiveJudgment() != null
            && JudgmentState.ISSUED.equals(caseData.getActiveJudgment().getState()));
    }

    private boolean isCompanyOrOrganisationWithRepaymentPlanAccepted(CaseData caseData) {
        return caseData.getRespondent1().isCompanyOROrganisation()
            && caseData.hasApplicantAcceptedRepaymentPlan();
    }
}
