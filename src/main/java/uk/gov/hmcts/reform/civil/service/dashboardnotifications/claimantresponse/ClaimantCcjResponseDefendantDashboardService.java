package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_COURT_AGREE_WITH_CLAIMANT_CCJ_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_CCJ_REJECT_PLAN_COURT_FAVOURS_DEFENDANT;

@Service
public class ClaimantCcjResponseDefendantDashboardService extends DashboardScenarioService {

    private final FeatureToggleService featureToggleService;

    public ClaimantCcjResponseDefendantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                        DashboardNotificationsParamsMapper mapper,
                                                        FeatureToggleService featureToggleService) {
        super(dashboardScenariosService, mapper);
        this.featureToggleService = featureToggleService;
    }

    public void notifyDefendant(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        ClaimantLiPResponse applicant1Response = Optional.ofNullable(caseData.getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .orElse(null);
        boolean isCcjRequested = applicant1Response != null
            && applicant1Response.hasApplicant1RequestedCcj();
        if (caseData.hasApplicantAcceptedRepaymentPlan() && isCcjRequested) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_REQUESTED_CCJ_CLAIMANT_ACCEPTED_DEFENDANT_PLAN_DEFENDANT.getScenario();
        } else if (caseData.hasApplicant1CourtDecisionInFavourOfClaimant() && caseData.isCcjRequestJudgmentByAdmission()) {
            return SCENARIO_AAA6_CLAIMANT_COURT_AGREE_WITH_CLAIMANT_CCJ_DEFENDANT.getScenario();
        } else if (caseData.hasApplicant1CourtDecisionInFavourOfDefendant() && caseData.hasApplicant1AcceptedCourtDecision()) {
            return SCENARIO_AAA6_CLAIMANT_INTENT_REQUEST_CCJ_REJECT_PLAN_COURT_FAVOURS_DEFENDANT
                .getScenario();
        }
        return null;
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return featureToggleService.isLipVLipEnabled() && caseData.isRespondent1NotRepresented();
    }
}
