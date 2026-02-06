package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.finalorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.finalorder.FinalOrderRespondentDashboardService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.GaDashboardServiceTask;

@Component
public class FinalOrderRespondentDashboardTask extends GaDashboardServiceTask {

    private final FinalOrderRespondentDashboardService dashboardService;

    public FinalOrderRespondentDashboardTask(FinalOrderRespondentDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(GeneralApplicationCaseData caseData, String authToken) {
        dashboardService.notifyFinalOrder(caseData, authToken);
    }
}
