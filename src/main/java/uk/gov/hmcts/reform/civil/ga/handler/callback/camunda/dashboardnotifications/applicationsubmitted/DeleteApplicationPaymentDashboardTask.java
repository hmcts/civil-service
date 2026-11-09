package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationsubmitted;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.GaDashboardServiceTask;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardNotificationHelper;

@Component
@RequiredArgsConstructor
public class DeleteApplicationPaymentDashboardTask extends GaDashboardServiceTask {

    private static final String APPLICANT_ROLE = "APPLICANT";
    private final DashboardNotificationHelper dashboardNotificationHelper;
    private final DashboardApiClient dashboardApiClient;
    private static final String PAYMENT_NOTIFICATION = "Notice.AAA6.GeneralApps.ApplicationFeeRequired.Applicant";
    private final GaForLipService gaForLipService;

    @Override
    protected void notifyDashboard(GeneralApplicationCaseData caseData, String authToken) {
        if (dashboardNotificationHelper.isAwaitingWelshTranslation(caseData)
            && gaForLipService.isGaForLip(caseData) && gaForLipService.isLipApp(caseData)) {
            dashboardApiClient.deleteTemplateNotificationsForCaseIdentifierAndRole(
                caseData.getCcdCaseReference().toString(),
                APPLICANT_ROLE,
                PAYMENT_NOTIFICATION,
                authToken);
        }
    }
}
