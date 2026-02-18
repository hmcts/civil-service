package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialreadyrespondent1;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadyrespondent1.TrialReadyCheckRespondent1DefendantDashboardService;

@Component
public class TrialReadyCheckRespondent1DefendantDashboardTask extends DashboardServiceTask {

    private final TrialReadyCheckRespondent1DefendantDashboardService dashboardService;

    public TrialReadyCheckRespondent1DefendantDashboardTask(TrialReadyCheckRespondent1DefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyCaseTrialReadyCheckRespondent1(caseData, authToken);
    }
}
