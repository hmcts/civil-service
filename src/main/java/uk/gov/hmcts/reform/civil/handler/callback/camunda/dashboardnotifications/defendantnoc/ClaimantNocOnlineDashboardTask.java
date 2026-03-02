package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantnoc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantnoc.ClaimantNocOnlineDashboardService;

@Component
public class ClaimantNocOnlineDashboardTask extends DashboardServiceTask {

    private final ClaimantNocOnlineDashboardService dashboardService;

    public ClaimantNocOnlineDashboardTask(ClaimantNocOnlineDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyClaimant(caseData, authToken);
    }
}
