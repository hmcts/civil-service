package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trailreadycheck;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trailreadyservice.TrailReadyCheckDefendantDashboardService;

@Component
public class TrailReadyCheckDefendantDashboardTask extends DashboardServiceTask {

    private final TrailReadyCheckDefendantDashboardService dashboardService;

    public TrailReadyCheckDefendantDashboardTask(TrailReadyCheckDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyCaseTrailReadyCheck(caseData, authToken);
    }
}
