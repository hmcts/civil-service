package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponse.DefendantResponseClaimantDashboardService;

@Component
public class DefendantResponseClaimantDashboardTask extends DashboardServiceTask {

    private final DefendantResponseClaimantDashboardService dashboardService;

    public DefendantResponseClaimantDashboardTask(DefendantResponseClaimantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyDefendantResponse(caseData, authToken);
    }
}
