package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cosc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.cosc.InitiateCoscClaimantDashboardService;

@Component
public class InitiateCoscClaimantDashboardTask extends DashboardServiceTask {

    private final InitiateCoscClaimantDashboardService claimantDashboardService;

    public InitiateCoscClaimantDashboardTask(InitiateCoscClaimantDashboardService claimantDashboardService) {
        this.claimantDashboardService = claimantDashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        claimantDashboardService.notifyInitiateCosc(caseData, authToken);
    }
}
