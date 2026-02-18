package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialreadycheck;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadycheck.TrialReadyCheckClaimantDashboardService;

@Component
public class TrialReadyCheckClaimantDashboardTask extends DashboardServiceTask {

    private final TrialReadyCheckClaimantDashboardService dashboardService;

    public TrialReadyCheckClaimantDashboardTask(TrialReadyCheckClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyTrialReadyCheck(caseData, authToken);
    }
}
