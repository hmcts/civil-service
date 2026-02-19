package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.finalorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.finalorder.FinalOrderDefendantDashboardService;

@Component
public class FinalOrderDefendantDashboardTask extends DashboardServiceTask {

    private final FinalOrderDefendantDashboardService dashboardService;

    public FinalOrderDefendantDashboardTask(FinalOrderDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyFinalOrder(caseData, authToken);
    }
}
