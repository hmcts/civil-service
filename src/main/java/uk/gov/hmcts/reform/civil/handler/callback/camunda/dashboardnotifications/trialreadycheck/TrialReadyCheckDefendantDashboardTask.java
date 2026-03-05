package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialreadycheck;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadycheck.TrialReadyCheckDefendantDashboardService;

@Component
public class TrialReadyCheckDefendantDashboardTask extends DashboardServiceTask {

    private final TrialReadyCheckDefendantDashboardService dashboardService;

    public TrialReadyCheckDefendantDashboardTask(TrialReadyCheckDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyCaseTrialReadyCheck(caseData, authToken);
    }
}
