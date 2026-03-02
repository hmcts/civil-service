package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantnoc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantnoc.DefendantNocClaimantDashboardService;

@Component
public class DefendantNocClaimantDashboardTask extends DashboardServiceTask {

    private final DefendantNocClaimantDashboardService dashboardService;

    public DefendantNocClaimantDashboardTask(DefendantNocClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyClaimant(caseData, authToken);
    }
}
