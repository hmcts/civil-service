package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.courtofficerorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.courtofficerorder.CourtOfficerOrderClaimantDashboardService;

@Component
public class CourtOfficerOrderClaimantDashboardTask extends DashboardServiceTask {

    private final CourtOfficerOrderClaimantDashboardService dashboardService;

    public CourtOfficerOrderClaimantDashboardTask(CourtOfficerOrderClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyCourtOfficerOrder(caseData, authToken);
    }
}
