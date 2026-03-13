package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimissue;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimissue.CreateClaimAfterPaymentDefendantDashboardService;

@Component
public class CreateClaimAfterPaymentDefendantDashboardTask extends DashboardServiceTask {

    private final CreateClaimAfterPaymentDefendantDashboardService dashboardService;

    public CreateClaimAfterPaymentDefendantDashboardTask(
        CreateClaimAfterPaymentDefendantDashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyDefendant(caseData, authToken);
    }
}
