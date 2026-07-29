package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimdismissed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimdismissed.ClaimDismissedClaimantDashboardService;

@Component
public class ClaimDismissedClaimantDashboardTask extends DashboardServiceTask {

    private final ClaimDismissedClaimantDashboardService dashboardService;

    public ClaimDismissedClaimantDashboardTask(ClaimDismissedClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyClaimDismissed(caseData, authToken);
    }
}
