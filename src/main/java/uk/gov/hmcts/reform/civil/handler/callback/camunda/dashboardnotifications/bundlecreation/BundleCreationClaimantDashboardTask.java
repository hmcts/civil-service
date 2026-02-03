package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.bundlecreation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.bundlecreation.BundleCreationClaimantDashboardService;

@Component
public class BundleCreationClaimantDashboardTask extends DashboardServiceTask {

    private final BundleCreationClaimantDashboardService dashboardService;

    public BundleCreationClaimantDashboardTask(BundleCreationClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyBundleCreated(caseData, authToken);
    }
}
