package uk.gov.hmcts.reform.civil.service.dashboardnotifications.cancelunissuedclaimspec;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CANCEL_UNISSUED_CLAIM_SPEC_CLAIMANT;

@Service
public class CancelUnissuedClaimSpecClaimantDashboardService extends DashboardScenarioService {

    public CancelUnissuedClaimSpecClaimantDashboardService(DashboardScenariosService dashboardScenariosService,
                                                           DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifyCancelUnissuedClaimSpec(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    protected String getScenario(CaseData caseData) {
        return SCENARIO_AAA6_CANCEL_UNISSUED_CLAIM_SPEC_CLAIMANT.getScenario();
    }

    @Override
    protected boolean shouldRecordScenario(CaseData caseData) {
        return caseData.isApplicantLiP();
    }
}
