package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.bundlecreation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.bundlecreation.BundleCreationDefendantDashboardService;

@Component
public class BundleCreationDefendantDashboardTask extends DashboardServiceTask {

    private final BundleCreationDefendantDashboardService dashboardService;

    public BundleCreationDefendantDashboardTask(BundleCreationDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyBundleCreated(caseData, authToken);
    }
}
