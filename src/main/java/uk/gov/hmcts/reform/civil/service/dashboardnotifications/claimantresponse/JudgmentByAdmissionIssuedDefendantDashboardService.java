package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_JUDGEMENTS_ONLINE_ISSUED_CCJ_DEFENDANT;

@Service
public class JudgmentByAdmissionIssuedDefendantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;

    public JudgmentByAdmissionIssuedDefendantDashboardService(
        DashboardScenariosService dashboardScenariosService,
        DashboardNotificationsParamsMapper mapper,
        FeatureToggleService featureToggleService
    ) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
    }

    public void notifyDefendant(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        if (caseData.isRespondent1LiP()) {
            if (isJudgmentOrderIssued(caseData)) {
                return SCENARIO_AAA6_JUDGEMENTS_ONLINE_ISSUED_CCJ_DEFENDANT.getScenario();
            } else if (caseData.hasApplicantAcceptedRepaymentPlan()
                && (caseData.isPayByInstallment() || caseData.isPayBySetDate())) {
                return SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT.getScenario();
            }
        }
        return null;
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return featureToggleService.isJudgmentOnlineLive();
    }

    private boolean isJudgmentOrderIssued(CaseData caseData) {
        if (caseData.isLRvLipOneVOne() && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null) {
            return true;
        }
        return caseData.isLipvLipOneVOne()
            && (isIndividualOrSoleTraderWithJoIssued(caseData)
            || isCompanyOrOrganisationWithRepaymentPlanAccepted(caseData));
    }

    private boolean isIndividualOrSoleTraderWithJoIssued(CaseData caseData) {
        return caseData.getRespondent1().isIndividualORSoleTrader()
            && caseData.getActiveJudgment() != null
            && JudgmentState.ISSUED.equals(caseData.getActiveJudgment().getState());
    }

    private boolean isCompanyOrOrganisationWithRepaymentPlanAccepted(CaseData caseData) {
        return caseData.getRespondent1().isCompanyOROrganisation()
            && caseData.hasApplicantAcceptedRepaymentPlan();
    }
}
