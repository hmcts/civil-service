package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.gentrialreadydocapplicant;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.gentrialreadydocapplicant.GenerateTrialReadyDocApplicantDashboardService;

import org.springframework.stereotype.Component;

@Component
public class GenerateTrialReadyDocApplicantDashboardTask extends DashboardServiceTask {

    private final GenerateTrialReadyDocApplicantDashboardService dashboardService;

    public GenerateTrialReadyDocApplicantDashboardTask(GenerateTrialReadyDocApplicantDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyGenerateTrialReadyDocApplicant(caseData, authToken);
    }
}
