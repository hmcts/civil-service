package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications.applicationsubmitted;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardNotificationHelper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteApplicationPaymentDashboardTaskTest {

    @Mock
    private DashboardNotificationHelper dashboardNotificationHelper;

    @Mock
    private DashboardApiClient client;
    @Mock
    private GaForLipService gaForLipService;

    @Test
    void shouldDeletePaymentNotificationForWelshLipApplicant() {
        GeneralApplicationCaseData caseData = welshSubmission();
        when(caseData.getCcdCaseReference()).thenReturn(123L);
        when(gaForLipService.isGaForLip(caseData)).thenReturn(true);
        when(gaForLipService.isLipApp(caseData)).thenReturn(true);

        execute(caseData);

        verify(client).deleteTemplateNotificationsForCaseIdentifierAndRole("123", "APPLICANT",
            "Notice.AAA6.GeneralApps.ApplicationFeeRequired.Applicant", "token");
    }

    @Test
    void shouldSkipNonLipApplication() {
        execute(welshSubmission());

        verifyNoInteractions(client);
    }

    @Test
    void shouldSkipRepresentedApplicantInLipApplication() {
        GeneralApplicationCaseData caseData = welshSubmission();
        when(gaForLipService.isGaForLip(caseData)).thenReturn(true);

        execute(caseData);

        verifyNoInteractions(client);
    }

    @Test
    void shouldSkipWhenApplicationIsNotAwaitingWelshTranslation() {
        GeneralApplicationCaseData caseData = mock(GeneralApplicationCaseData.class);

        execute(caseData);

        verifyNoInteractions(client, gaForLipService);
    }

    private GeneralApplicationCaseData welshSubmission() {
        GeneralApplicationCaseData caseData = mock(GeneralApplicationCaseData.class);
        when(dashboardNotificationHelper.isAwaitingWelshTranslation(caseData)).thenReturn(true);
        return caseData;
    }

    private void execute(GeneralApplicationCaseData caseData) {
        new DeleteApplicationPaymentDashboardTask(dashboardNotificationHelper, client, gaForLipService)
            .execute(DashboardTaskContext.generalApplication(caseData, "token"));
    }
}
