package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.makedecision;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications.makedecision.MakeDecisionApplicantDashboardService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.GaDashboardServiceTask;

@Component
public class MakeDecisionApplicantDashboardTask extends GaDashboardServiceTask {

    private final MakeDecisionApplicantDashboardService dashboardService;

    public MakeDecisionApplicantDashboardTask(MakeDecisionApplicantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(GeneralApplicationCaseData caseData, String authToken) {
        dashboardService.notifyMakeDecision(caseData, authToken);
    }
}
