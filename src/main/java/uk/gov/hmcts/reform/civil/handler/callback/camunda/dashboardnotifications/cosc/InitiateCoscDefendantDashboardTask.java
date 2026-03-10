package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.cosc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.GaDashboardServiceTask;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.cosc.InitiateCoscDefendantDashboardService;

@Component
public class InitiateCoscDefendantDashboardTask extends GaDashboardServiceTask {

    private final InitiateCoscDefendantDashboardService defendantDashboardService;

    public InitiateCoscDefendantDashboardTask(InitiateCoscDefendantDashboardService defendantDashboardService) {
        this.defendantDashboardService = defendantDashboardService;
    }

    @Override
    protected void notifyDashboard(GeneralApplicationCaseData caseData, String authToken) {
        defendantDashboardService.notifyInitiateCosc(caseData, authToken);
    }
}
