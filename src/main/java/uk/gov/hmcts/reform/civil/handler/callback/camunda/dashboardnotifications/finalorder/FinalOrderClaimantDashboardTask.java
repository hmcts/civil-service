package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.finalorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.finalorder.FinalOrderClaimantDashboardService;

@Component
public class FinalOrderClaimantDashboardTask extends DashboardServiceTask {

    private final FinalOrderClaimantDashboardService dashboardService;

    public FinalOrderClaimantDashboardTask(FinalOrderClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyFinalOrder(caseData, authToken);
    }
}
