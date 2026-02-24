package uk.gov.hmcts.reform.civil.service.dashboardnotifications.settleclaimmarkedpaidinfull;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardScenarioService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_SETTLE_CLAIM_PAID_IN_FULL_DEFENDANT;

@Service
public class SettleClaimMarkedPaidInFullDashboardService extends DashboardScenarioService {

    public SettleClaimMarkedPaidInFullDashboardService(DashboardScenariosService dashboardScenariosService, DashboardNotificationsParamsMapper mapper) {
        super(dashboardScenariosService, mapper);
    }

    public void notifySettleClaimMarkedPaidInFull(CaseData caseData, String authToken) {
        recordScenario(caseData, authToken);
    }

    @Override
    public String getScenario(CaseData caseData) {
        if (caseData.isRespondent1LiP()) {
            return SCENARIO_AAA6_SETTLE_CLAIM_PAID_IN_FULL_DEFENDANT.getScenario();
        }
        return null;
    }
}
