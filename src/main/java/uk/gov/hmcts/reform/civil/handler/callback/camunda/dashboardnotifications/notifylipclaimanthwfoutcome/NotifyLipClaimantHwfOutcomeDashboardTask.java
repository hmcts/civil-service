package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.notifylipclaimanthwfoutcome;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.notifylipclaimanthwfoutcome.NotifyLipClaimantHwfOutcomeDashboardService;

@Component
public class NotifyLipClaimantHwfOutcomeDashboardTask extends DashboardServiceTask {

    private final NotifyLipClaimantHwfOutcomeDashboardService dashboardService;

    public NotifyLipClaimantHwfOutcomeDashboardTask(NotifyLipClaimantHwfOutcomeDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyNotifyLipClaimantHwfOutcome(caseData, authToken);
    }
}
