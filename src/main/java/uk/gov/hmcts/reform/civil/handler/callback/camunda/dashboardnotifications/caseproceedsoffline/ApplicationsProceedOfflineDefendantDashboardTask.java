package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline.ApplicationsProceedOfflineDefendantDashboardService;

@Component
public class ApplicationsProceedOfflineDefendantDashboardTask extends DashboardServiceTask {

    private final ApplicationsProceedOfflineDefendantDashboardService dashboardService;

    public ApplicationsProceedOfflineDefendantDashboardTask(ApplicationsProceedOfflineDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notify(caseData, authToken);
    }
}
