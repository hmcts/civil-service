package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationissued;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.applicationissued.ApplicationIssuedRespondentDashboardService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.GaDashboardServiceTask;

@Component
public class ApplicationIssuedRespondentDashboardTask extends GaDashboardServiceTask {

    private final ApplicationIssuedRespondentDashboardService dashboardService;

    public ApplicationIssuedRespondentDashboardTask(ApplicationIssuedRespondentDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(GeneralApplicationCaseData caseData, String authToken) {
        dashboardService.notifyApplicationIssued(caseData, authToken);
    }
}
