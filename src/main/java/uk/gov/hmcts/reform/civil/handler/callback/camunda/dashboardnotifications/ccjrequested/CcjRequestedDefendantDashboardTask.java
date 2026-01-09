package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.ccjrequested;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.ccjrequested.CcjRequestedDefendantDashboardService;

@Component
public class CcjRequestedDefendantDashboardTask extends DashboardServiceTask {

    private final CcjRequestedDefendantDashboardService dashboardService;

    public CcjRequestedDefendantDashboardTask(CcjRequestedDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyDefendant(caseData, authToken);
    }
}
