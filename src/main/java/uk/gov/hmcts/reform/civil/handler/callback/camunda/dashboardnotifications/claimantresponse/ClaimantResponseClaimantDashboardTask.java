package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse.ClaimantResponseClaimantDashboardService;

@Component
public class ClaimantResponseClaimantDashboardTask extends DashboardServiceTask {

    private final ClaimantResponseClaimantDashboardService dashboardService;

    public ClaimantResponseClaimantDashboardTask(ClaimantResponseClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyClaimant(caseData, authToken);
    }
}
