package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cancelunissuedclaimspec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.cancelunissuedclaimspec.CancelUnissuedClaimSpecClaimantDashboardService;

@Component
public class CancelUnissuedClaimSpecClaimantDashboardTask extends DashboardServiceTask {

    private final CancelUnissuedClaimSpecClaimantDashboardService dashboardService;

    public CancelUnissuedClaimSpecClaimantDashboardTask(CancelUnissuedClaimSpecClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyCancelUnissuedClaimSpec(caseData, authToken);
    }
}
