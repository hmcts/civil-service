package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.casedismiss;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.casedismissed.CaseDismissDefendantDashboardService;

@Component
public class CaseDismissDefendantDashboardTask extends DashboardServiceTask {

    private final CaseDismissDefendantDashboardService dashboardService;

    public CaseDismissDefendantDashboardTask(CaseDismissDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyCaseDismissed(caseData, authToken);
    }
}
