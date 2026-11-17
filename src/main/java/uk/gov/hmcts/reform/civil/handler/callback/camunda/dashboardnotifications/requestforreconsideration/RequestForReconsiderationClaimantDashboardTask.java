package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.requestforreconsideration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.requestforreconsideration.RequestForReconsiderationClaimantDashboardService;

@Component
public class RequestForReconsiderationClaimantDashboardTask extends DashboardServiceTask {

    private final RequestForReconsiderationClaimantDashboardService dashboardService;

    public RequestForReconsiderationClaimantDashboardTask(RequestForReconsiderationClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyRequestForReconsideration(caseData, authToken);
    }
}
