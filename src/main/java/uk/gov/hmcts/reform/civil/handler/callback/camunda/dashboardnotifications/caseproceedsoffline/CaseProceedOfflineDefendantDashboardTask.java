package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline.CaseProceedOfflineDefendantDashboardService;

@Component
public class CaseProceedOfflineDefendantDashboardTask extends DashboardServiceTask {

    private final CaseProceedOfflineDefendantDashboardService dashboardService;

    public CaseProceedOfflineDefendantDashboardTask(CaseProceedOfflineDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyCaseProceedOffline(caseData, authToken);
    }
}
