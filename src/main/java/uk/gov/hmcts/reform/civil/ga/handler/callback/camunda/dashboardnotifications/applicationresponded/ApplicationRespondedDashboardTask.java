package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationresponded;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationresponded.ApplicationRespondedDashboardService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.GaDashboardServiceTask;

@Component
public class ApplicationRespondedDashboardTask extends GaDashboardServiceTask {

    private final ApplicationRespondedDashboardService dashboardService;

    public ApplicationRespondedDashboardTask(ApplicationRespondedDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(GeneralApplicationCaseData caseData, String authToken) {
        dashboardService.notifyApplicationResponded(caseData, authToken);
    }
}
