package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineClaimantDashboardService;

@Component
public class ApplicationsProceedOfflineClaimantDashboardTask extends DashboardServiceTask {

    private final ApplicationsProceedOfflineClaimantDashboardService dashboardService;

    public ApplicationsProceedOfflineClaimantDashboardTask(ApplicationsProceedOfflineClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notify(caseData, authToken);
    }
}
