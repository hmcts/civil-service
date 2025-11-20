package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.dismisscase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.dismisscase.DismissCaseDefendantDashboardService;

@Component
public class DismissCaseDefendantDashboardTask extends DashboardServiceTask {

    private final DismissCaseDefendantDashboardService dashboardService;

    public DismissCaseDefendantDashboardTask(DismissCaseDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyCaseDismissed(caseData, authToken);
    }
}
