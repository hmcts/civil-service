package uk.gov.hmcts.reform.civil.service.dashboardnotifications.discontinueclaimclaimant;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DISCONTINUE_NOTICE_OF_DISCONTINUE_ISSUED_DEFENDANT;

@Service
public class DiscontinueClaimClaimantDashboardService extends DashboardScenarioService {

    public DiscontinueClaimClaimantDashboardService(DashboardScenariosService dashboardScenariosService, DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifyDiscontinueClaimClaimant(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (caseData.isRespondent1LiP()) {
            return SCENARIO_AAA6_DISCONTINUE_NOTICE_OF_DISCONTINUE_ISSUED_DEFENDANT.getScenario();
        }
        return null;
    }
}
