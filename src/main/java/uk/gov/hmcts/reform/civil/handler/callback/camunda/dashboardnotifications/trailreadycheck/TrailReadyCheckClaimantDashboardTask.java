package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trailreadycheck;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trailreadycheck.TrailReadyCheckClaimantDashboardService;

@Component
public class TrailReadyCheckClaimantDashboardTask extends DashboardServiceTask {

    private final TrailReadyCheckClaimantDashboardService dashboardService;

    public TrailReadyCheckClaimantDashboardTask(TrailReadyCheckClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyTrailReadyCheck(caseData, authToken);
    }
}
