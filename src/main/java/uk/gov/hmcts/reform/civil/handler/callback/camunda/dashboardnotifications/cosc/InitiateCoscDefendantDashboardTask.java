package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cosc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.cosc.InitiateCoscDefendantDashboardService;

@Component
public class InitiateCoscDefendantDashboardTask extends DashboardServiceTask {

    private final InitiateCoscDefendantDashboardService defendantDashboardService;

    public InitiateCoscDefendantDashboardTask(InitiateCoscDefendantDashboardService defendantDashboardService) {
        this.defendantDashboardService = defendantDashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        defendantDashboardService.notifyInitiateCosc(caseData, authToken);
    }
}
