package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimissue;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimissue.CreateClaimAfterPaymentApplicantDashboardService;

@Component
public class CreateClaimAfterPaymentApplicantDashboardTask extends DashboardServiceTask {

    private final CreateClaimAfterPaymentApplicantDashboardService dashboardService;

    public CreateClaimAfterPaymentApplicantDashboardTask(
        CreateClaimAfterPaymentApplicantDashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyClaimant(caseData, authToken);
    }
}
