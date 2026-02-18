package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.trialarrangementsnotifyotherparty;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.trialarrangementsnotifyotherparty.TrialArrangementsNotifyOtherPartyClaimantDashboardService;

@Component
public class TrialArrangementsNotifyOtherPartyClaimantDashboardTask extends DashboardServiceTask {

    private final TrialArrangementsNotifyOtherPartyClaimantDashboardService dashboardService;

    public TrialArrangementsNotifyOtherPartyClaimantDashboardTask(TrialArrangementsNotifyOtherPartyClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyTrialArrangementsNotifyOtherParty(caseData, authToken);
    }
}
