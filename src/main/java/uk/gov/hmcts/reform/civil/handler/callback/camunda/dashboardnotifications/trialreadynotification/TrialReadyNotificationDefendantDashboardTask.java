package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialreadynotification;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadynotification.TrialReadyNotificationDefendantDashboardService;

import org.springframework.stereotype.Component;

@Component
public class TrialReadyNotificationDefendantDashboardTask extends DashboardServiceTask {

    private final TrialReadyNotificationDefendantDashboardService dashboardService;

    public TrialReadyNotificationDefendantDashboardTask(TrialReadyNotificationDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyTrialReadyNotification(caseData, authToken);
    }
}
