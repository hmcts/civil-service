package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.hwf;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.hwf.HwfOutcomeApplicantDashboardService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.GaDashboardServiceTask;

@Component
public class HwfOutcomeApplicantDashboardTask extends GaDashboardServiceTask {

    private final HwfOutcomeApplicantDashboardService dashboardService;

    public HwfOutcomeApplicantDashboardTask(HwfOutcomeApplicantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(GeneralApplicationCaseData caseData, String authToken) {
        dashboardService.notifyHwfOutcome(caseData, authToken);
    }
}
