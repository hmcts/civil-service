package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimissue;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimissue.CreateClaimAfterPaymentApplicantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardNotificationHelper;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;

@Component
public class CreateClaimAfterPaymentApplicantDashboardTask extends DashboardServiceTask {

    private final DashboardNotificationHelper dashboardNotificationHelper;
    private final CreateClaimAfterPaymentApplicantDashboardService dashboardService;

    public CreateClaimAfterPaymentApplicantDashboardTask(
        CreateClaimAfterPaymentApplicantDashboardService dashboardService,
        DashboardNotificationHelper dashboardNotificationHelper
    ) {
        this.dashboardService = dashboardService;
        this.dashboardNotificationHelper = dashboardNotificationHelper;
    }

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        if (!dashboardNotificationHelper.isAwaitingWelshTranslation(caseData, CREATE_CLAIM_SPEC_AFTER_PAYMENT)) {
            dashboardService.notifyClaimant(caseData, authToken);
        }
    }
}
