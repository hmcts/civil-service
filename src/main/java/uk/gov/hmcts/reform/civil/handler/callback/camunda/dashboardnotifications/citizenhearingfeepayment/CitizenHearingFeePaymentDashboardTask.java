package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.citizenhearingfeepayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.citizenhearingfeepayment.CitizenHearingFeePaymentDashboardService;

@Component
public class CitizenHearingFeePaymentDashboardTask extends DashboardServiceTask {

    private final CitizenHearingFeePaymentDashboardService dashboardService;

    public CitizenHearingFeePaymentDashboardTask(CitizenHearingFeePaymentDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        dashboardService.notifyCitizenHearingFeePayment(caseData, authToken);
    }
}
