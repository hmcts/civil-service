package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.ccjrequested;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.ccjrequested.CcjRequestedClaimantDashboardService;

@Component
public class CcjRequestedClaimantDashboardTask extends DashboardServiceTask {

    private final CcjRequestedClaimantDashboardService dashboardService;

    public CcjRequestedClaimantDashboardTask(CcjRequestedClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyClaimant(caseData, authToken);
    }
}
