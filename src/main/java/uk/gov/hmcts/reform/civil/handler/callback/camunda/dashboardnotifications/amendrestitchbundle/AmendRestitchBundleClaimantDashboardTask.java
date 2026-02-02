package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.amendrestitchbundle;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.amendrestitchbundle.AmendRestitchBundleClaimantDashboardService;

@Component
public class AmendRestitchBundleClaimantDashboardTask extends DashboardServiceTask {

    private final AmendRestitchBundleClaimantDashboardService dashboardService;

    public AmendRestitchBundleClaimantDashboardTask(AmendRestitchBundleClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyBundleUpdated(caseData, authToken);
    }
}
