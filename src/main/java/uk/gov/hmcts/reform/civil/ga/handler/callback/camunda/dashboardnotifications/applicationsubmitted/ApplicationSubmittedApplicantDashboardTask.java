package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationsubmitted;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationsubmitted.ApplicationSubmittedApplicantDashboardService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.GaDashboardServiceTask;

@Component
public class ApplicationSubmittedApplicantDashboardTask extends GaDashboardServiceTask {

    private final ApplicationSubmittedApplicantDashboardService dashboardService;

    public ApplicationSubmittedApplicantDashboardTask(ApplicationSubmittedApplicantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(GeneralApplicationCaseData caseData, String authToken) {
        dashboardService.notifyApplicationSubmitted(caseData, authToken);
    }
}
