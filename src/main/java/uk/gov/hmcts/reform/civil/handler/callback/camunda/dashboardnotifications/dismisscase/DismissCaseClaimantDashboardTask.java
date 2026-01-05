package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.dismisscase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.dismisscase.DismissCaseClaimantDashboardService;

@Component
public class DismissCaseClaimantDashboardTask extends DashboardServiceTask {

    private final DismissCaseClaimantDashboardService dashboardService;

    public DismissCaseClaimantDashboardTask(DismissCaseClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyCaseDismissed(caseData, authToken);
    }
}
