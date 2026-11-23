package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimissue;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardNotificationHelper;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC_AFTER_PAYMENT;

@Component
@RequiredArgsConstructor
public class RemovePaymentDashboardTask extends DashboardServiceTask {

    private static final String PAYMENT_NOTIFICATION = "Notice.AAA6.ClaimIssue.ClaimFee.Required";
    private static final String CLAIMANT_ROLE = "CLAIMANT";
    private final DashboardNotificationHelper dashboardNotificationHelper;
    private final DashboardNotificationService dashboardNotificationService;

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        if (!dashboardNotificationHelper.isAwaitingWelshTranslation(caseData, CREATE_CLAIM_SPEC_AFTER_PAYMENT)) {
            return;
        }
        dashboardNotificationService.deleteByNameAndReferenceAndCitizenRole(PAYMENT_NOTIFICATION,
                                                                            String.valueOf(caseData.getCcdCaseReference()), CLAIMANT_ROLE);
    }
}
