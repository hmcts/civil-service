package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialreadynotification;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialreadynotification.TrialReadyNotificationClaimantDashboardService;

import org.springframework.stereotype.Component;

@Component
public class TrialReadyNotificationClaimantDashboardTask extends DashboardServiceTask {

    private TrialReadyNotificationClaimantDashboardService dashboardService;

    public TrialReadyNotificationClaimantDashboardTask(TrialReadyNotificationClaimantDashboardService service) {
        this.dashboardService = service;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyTrialReadyNotification(caseData, authToken);
    }
}
