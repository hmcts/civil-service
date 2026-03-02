package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse.ClaimantCcjResponseDefendantDashboardService;

@Component
public class ClaimantCcjResponseDefendantDashboardTask extends DashboardServiceTask {

    private final ClaimantCcjResponseDefendantDashboardService dashboardService;

    public ClaimantCcjResponseDefendantDashboardTask(
        ClaimantCcjResponseDefendantDashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyDefendant(caseData, authToken);
    }
}
