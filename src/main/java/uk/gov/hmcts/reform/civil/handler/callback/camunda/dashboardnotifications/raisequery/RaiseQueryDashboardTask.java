package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.raisequery;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.raisequery.RaiseQueryDashboardService;

@Component
public class RaiseQueryDashboardTask extends DashboardServiceTask  {

    private final RaiseQueryDashboardService dashboardService;

    public RaiseQueryDashboardTask(RaiseQueryDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyRaiseQuery(caseData, authToken);
    }
}
