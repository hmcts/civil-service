package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.amendrestitchbundle;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.amendrestitchbundle.AmendRestitchBundleDefendantDashboardService;

@Component
public class AmendRestitchBundleDefendantDashboardTask extends DashboardServiceTask {

    private final AmendRestitchBundleDefendantDashboardService dashboardService;

    public AmendRestitchBundleDefendantDashboardTask(AmendRestitchBundleDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyBundleUpdated(caseData, authToken);
    }
}
