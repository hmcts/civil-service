package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.hearingscheduled;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingscheduled.HearingScheduledDefendantDashboardService;

public class HearingScheduledDefendantDashboardTask extends DashboardServiceTask {

    private final HearingScheduledDefendantDashboardService dashboardService;

    public HearingScheduledDefendantDashboardTask(HearingScheduledDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyHearingScheduled(caseData, authToken);
    }
}
