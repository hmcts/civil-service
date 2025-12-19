package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.caseproceedsoffline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline.CaseProceedOfflineClaimantDashboardService;

@Component
public class CaseProceedOfflineClaimantDashboardTask extends DashboardServiceTask {

    private final CaseProceedOfflineClaimantDashboardService dashboardService;

    public CaseProceedOfflineClaimantDashboardTask(CaseProceedOfflineClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyCaseProceedOffline(caseData, authToken);
    }
}
