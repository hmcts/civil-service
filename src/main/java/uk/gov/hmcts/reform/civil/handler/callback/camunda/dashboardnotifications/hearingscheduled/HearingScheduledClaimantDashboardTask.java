package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.hearingscheduled;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.dismisscase.DismissCaseClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingscheduled.HearingScheduledClaimantDashboardService;

@Component
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
