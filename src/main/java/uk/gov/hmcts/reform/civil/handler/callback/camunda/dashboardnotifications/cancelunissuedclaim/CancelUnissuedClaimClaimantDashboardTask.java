package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cancelunissuedclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.cancelunissuedclaim.CancelUnissuedClaimClaimantDashboardService;

@Component
public class CancelUnissuedClaimClaimantDashboardTask extends DashboardServiceTask {

    private final CancelUnissuedClaimClaimantDashboardService dashboardService;

    public CancelUnissuedClaimClaimantDashboardTask(CancelUnissuedClaimClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyCancelUnissuedClaim(caseData, authToken);
    }
}
