package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.createsdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.createsdo.CreateSdoDefendantDashboardService;

@Component
public class CreateSdoDefendantDashboardTask extends DashboardServiceTask {

    private final CreateSdoDefendantDashboardService dashboardService;

    public CreateSdoDefendantDashboardTask(CreateSdoDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyBundleUpdated(caseData, authToken);
    }
}
