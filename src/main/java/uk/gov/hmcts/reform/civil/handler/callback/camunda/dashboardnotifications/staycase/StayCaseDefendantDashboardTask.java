package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.staycase;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.staycase.StayCaseDefendantDashboardService;

import org.springframework.stereotype.Component;

@Component
public class StayCaseDefendantDashboardTask extends DashboardServiceTask {

    private final StayCaseDefendantDashboardService dashboardService;

    public StayCaseDefendantDashboardTask(StayCaseDefendantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyStayCase(caseData, authToken);
    }
}
