package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimsettled;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimsettled.ClaimSettledClaimantDashboardService;

@Component
public class ClaimSettledClaimantDashboardTask extends DashboardServiceTask {

    private final ClaimSettledClaimantDashboardService dashboardService;

    public ClaimSettledClaimantDashboardTask(ClaimSettledClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyClaimSettled(caseData, authToken);
    }
}
