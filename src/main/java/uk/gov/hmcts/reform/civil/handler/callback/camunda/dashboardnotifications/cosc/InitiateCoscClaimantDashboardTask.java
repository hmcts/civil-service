package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cosc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.GaDashboardServiceTask;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.cosc.InitiateCoscClaimantDashboardService;

@Component
public class InitiateCoscClaimantDashboardTask extends GaDashboardServiceTask {

    private final InitiateCoscClaimantDashboardService claimantDashboardService;

    public InitiateCoscClaimantDashboardTask(InitiateCoscClaimantDashboardService claimantDashboardService) {
        this.claimantDashboardService = claimantDashboardService;
    }

    @Override
    protected void notifyDashboard(GeneralApplicationCaseData caseData, String authToken) {
        claimantDashboardService.notifyInitiateCosc(caseData, authToken);
    }
}
