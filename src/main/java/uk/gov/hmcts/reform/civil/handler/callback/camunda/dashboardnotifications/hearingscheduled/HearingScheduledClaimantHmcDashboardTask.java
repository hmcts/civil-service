package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.hearingscheduled;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingscheduled.HearingScheduledClaimantHmcDashboardService;

@Component
public class HearingScheduledClaimantHmcDashboardTask extends DashboardServiceTask {

    private final HearingScheduledClaimantHmcDashboardService dashboardService;

    public HearingScheduledClaimantHmcDashboardTask(HearingScheduledClaimantHmcDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyHearingScheduled(caseData, authToken);
    }
}
