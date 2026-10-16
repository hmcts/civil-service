package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.requestforreconsideration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.requestforreconsideration.RequestForReconsiderationDefendantDashboardService;

@Component
public class RequestForReconsiderationDefendantDashboardTask extends DashboardServiceTask {

    private final RequestForReconsiderationDefendantDashboardService dashboardService;

    public RequestForReconsiderationDefendantDashboardTask(RequestForReconsiderationDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyRequestForReconsideration(caseData, authToken);
    }
}
