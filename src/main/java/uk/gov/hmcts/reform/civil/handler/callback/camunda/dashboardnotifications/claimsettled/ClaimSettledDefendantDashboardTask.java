package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimsettled;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimsettled.ClaimSettledDefendantDashboardService;

@Component
public class ClaimSettledDefendantDashboardTask extends DashboardServiceTask {

    private final ClaimSettledDefendantDashboardService dashboardService;

    public ClaimSettledDefendantDashboardTask(ClaimSettledDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyClaimSettled(caseData, authToken);
    }
}
