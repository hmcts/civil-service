package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.finalorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.finalorder.FinalOrderApplicantDashboardService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.GaDashboardServiceTask;

@Component
public class FinalOrderApplicantDashboardTask extends GaDashboardServiceTask {

    private final FinalOrderApplicantDashboardService dashboardService;

    public FinalOrderApplicantDashboardTask(FinalOrderApplicantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(GeneralApplicationCaseData caseData, String authToken) {
        dashboardService.notifyFinalOrder(caseData, authToken);
    }
}
