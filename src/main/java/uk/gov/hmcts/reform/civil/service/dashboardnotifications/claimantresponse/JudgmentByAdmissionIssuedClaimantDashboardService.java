package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_JUDGEMENTS_ONLINE_ISSUED_CCJ_CLAIMANT;

@Service
public class JudgmentByAdmissionIssuedClaimantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;

    public JudgmentByAdmissionIssuedClaimantDashboardService(
        DashboardScenariosService dashboardScenariosService,
        DashboardNotificationsParamsMapper mapper,
        FeatureToggleService featureToggleService
    ) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
    }

    public void notifyClaimant(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        if (isJudgmentOrderIssued(caseData)) {
            return SCENARIO_AAA6_UPDATE_JUDGEMENTS_ONLINE_ISSUED_CCJ_CLAIMANT.getScenario();
        }
        return null;
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return featureToggleService.isJudgmentOnlineLive();
    }

    private boolean isJudgmentOrderIssued(CaseData caseData) {
        return caseData.isApplicantLiP()
            && isActiveJudgmentExist(caseData)
            && (isIndividualOrSoleTraderWithJoIssued(caseData)
            || isCompanyOrOrganisationWithRepaymentPlanAccepted(caseData));
    }

    private boolean isActiveJudgmentExist(CaseData caseData) {
        return caseData.getActiveJudgment() != null
            && JudgmentState.ISSUED.equals(caseData.getActiveJudgment().getState())
            && JudgmentType.JUDGMENT_BY_ADMISSION.equals(caseData.getActiveJudgment().getType());
    }

    private boolean isIndividualOrSoleTraderWithJoIssued(CaseData caseData) {
        return caseData.getRespondent1().isIndividualORSoleTrader();
    }

    private boolean isCompanyOrOrganisationWithRepaymentPlanAccepted(CaseData caseData) {
        return caseData.getRespondent1().isCompanyOROrganisation()
            && caseData.hasApplicantAcceptedRepaymentPlan();
    }
}
