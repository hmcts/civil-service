package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.hearingscheduled;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingscheduled.HearingScheduledClaimantDashboardService;

public class HearingScheduledClaimantDashboardTask extends DashboardServiceTask {

    private final HearingScheduledClaimantDashboardService dashboardService;

    public HearingScheduledClaimantDashboardTask(HearingScheduledClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyHearingScheduled(caseData, authToken);
    }
}
