package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse.ClaimantResponseDefendantDashboardService;

@Component
public class ClaimantResponseDefendantDashboardTask extends DashboardServiceTask {

    private final ClaimantResponseDefendantDashboardService dashboardService;

    public ClaimantResponseDefendantDashboardTask(ClaimantResponseDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyDefendant(caseData, authToken);
    }
}
