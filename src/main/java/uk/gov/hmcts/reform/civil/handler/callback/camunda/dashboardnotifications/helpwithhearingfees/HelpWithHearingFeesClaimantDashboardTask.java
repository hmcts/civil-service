package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.helpwithhearingfees;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helpwithhearingfees.HelpWithHearingFeesClaimantDashboardService;

import org.springframework.stereotype.Component;

@Component
public class HelpWithHearingFeesClaimantDashboardTask extends DashboardServiceTask {

    private final HelpWithHearingFeesClaimantDashboardService dashboardService;

    public HelpWithHearingFeesClaimantDashboardTask(HelpWithHearingFeesClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyHelpWithHearingFees(caseData, authToken);
    }
}
