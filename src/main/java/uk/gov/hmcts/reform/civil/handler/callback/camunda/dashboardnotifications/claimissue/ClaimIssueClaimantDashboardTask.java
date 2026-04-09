package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimissue;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimissue.ClaimIssueClaimantDashboardService;

@Component
public class ClaimIssueClaimantDashboardTask extends DashboardServiceTask {

    private final ClaimIssueClaimantDashboardService dashboardService;

    public ClaimIssueClaimantDashboardTask(ClaimIssueClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyClaimIssue(caseData, authToken);
    }
}
