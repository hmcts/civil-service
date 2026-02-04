package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.createlipclaim;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.createlipclaim.CreateLipClaimDashboardService;

@Component
public class CreateLipClaimDashboardTask extends DashboardServiceTask {

    private final CreateLipClaimDashboardService dashboardService;

    public CreateLipClaimDashboardTask(CreateLipClaimDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyCreateLipClaim(caseData, authToken);
    }
}
