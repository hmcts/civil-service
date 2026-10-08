package uk.gov.hmcts.reform.civil.service.dashboardnotifications.cancelunissuedclaim;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CANCEL_UNISSUED_CLAIM_CLAIMANT;

@Service
public class CancelUnissuedClaimClaimantDashboardService extends DashboardScenarioService {

    public CancelUnissuedClaimClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                           DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifyCancelUnissuedClaim(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_CANCEL_UNISSUED_CLAIM_CLAIMANT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantLiP();
    }
}
