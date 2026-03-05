package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.respondtoquery;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.respondtoquery.RespondToQueryDashboardService;

@Component
public class RespondToQueryDashboardTask extends DashboardServiceTask {

    private final RespondToQueryDashboardService dashboardService;

    public RespondToQueryDashboardTask(RespondToQueryDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyRespondToQuery(caseData, authToken);
    }
}
