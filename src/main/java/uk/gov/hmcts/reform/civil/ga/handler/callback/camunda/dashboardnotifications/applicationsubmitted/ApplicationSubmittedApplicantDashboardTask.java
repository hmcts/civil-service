package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationsubmitted;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationsubmitted.ApplicationSubmittedApplicantDashboardService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.GaDashboardServiceTask;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardNotificationHelper;

@Component
public class ApplicationSubmittedApplicantDashboardTask extends GaDashboardServiceTask {

    private final DashboardNotificationHelper dashboardNotificationHelper;
    private final ApplicationSubmittedApplicantDashboardService dashboardService;

    public ApplicationSubmittedApplicantDashboardTask(ApplicationSubmittedApplicantDashboardService dashboardService,
                                                        DashboardNotificationHelper dashboardNotificationHelper) {
        this.dashboardService = dashboardService;
        this.dashboardNotificationHelper = dashboardNotificationHelper;
    }

    @Override
    protected void notifyDashboard(GeneralApplicationCaseData caseData, String authToken) {
        if (!dashboardNotificationHelper.isAwaitingWelshTranslation(caseData)) {
            dashboardService.notifyApplicationSubmitted(caseData, authToken);
        }
    }
}
