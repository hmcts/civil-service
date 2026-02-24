package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.settleclaimmarkedpaidinfull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.settleclaimmarkedpaidinfull.SettleClaimMarkedPaidInFullDashboardService;

@Component
public class SettleClaimMarkedPaidInFullDashboardTask extends DashboardServiceTask {

    private final SettleClaimMarkedPaidInFullDashboardService dashboardService;

    public SettleClaimMarkedPaidInFullDashboardTask(SettleClaimMarkedPaidInFullDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifySettleClaimMarkedPaidInFull(caseData, authToken);
    }
}
