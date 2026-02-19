package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.staylifted;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.staylifted.StayLiftedClaimantDashboardService;

@Component
public class StayLiftedClaimantDashboardTask extends DashboardServiceTask {

    private final StayLiftedClaimantDashboardService dashboardService;

    public StayLiftedClaimantDashboardTask(StayLiftedClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyStayLifted(caseData, authToken);
    }
}
