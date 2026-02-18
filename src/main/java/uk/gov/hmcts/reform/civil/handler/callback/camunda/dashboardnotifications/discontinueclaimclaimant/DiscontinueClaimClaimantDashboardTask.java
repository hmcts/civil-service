package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.discontinueclaimclaimant;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.discontinueclaimclaimant.DiscontinueClaimClaimantDashboardService;

@Component
public class DiscontinueClaimClaimantDashboardTask extends DashboardServiceTask {

    private final DiscontinueClaimClaimantDashboardService dashboardService;

    public DiscontinueClaimClaimantDashboardTask(DiscontinueClaimClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyDiscontinueClaimClaimant(caseData, authToken);
    }
}
