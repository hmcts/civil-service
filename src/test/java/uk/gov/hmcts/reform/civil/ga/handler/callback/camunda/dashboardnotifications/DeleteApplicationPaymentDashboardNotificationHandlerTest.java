package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DELETE_APPLICATION_PAYMENT_DASHBOARD_NOTIFICATION;

@ExtendWith(MockitoExtension.class)
public class DeleteApplicationPaymentDashboardNotificationHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @InjectMocks
    private DeleteApplicationPaymentDashboardNotificationHandler handler;

    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private GaForLipService gaForLipService;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(DELETE_APPLICATION_PAYMENT_DASHBOARD_NOTIFICATION);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRemoveNotification_whenInvoked() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            when(gaForLipService.isGaForLip(caseData)).thenReturn(true);
            when(gaForLipService.isLipApp(caseData)).thenReturn(true);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(DELETE_APPLICATION_PAYMENT_DASHBOARD_NOTIFICATION.name()).build()
            ).build();

            handler.handle(params);

            verify(dashboardApiClient).deleteTemplateNotificationsForCaseIdentifierAndRole(
                String.valueOf(caseData.getCcdCaseReference()),
                "APPLICANT",
                "Notice.AAA6.GeneralApps.ApplicationFeeRequired.Applicant",
                "BEARER_TOKEN"
            );
        }

        @Test
        void shouldNotRemoveNotification_whenInvoked() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(DELETE_APPLICATION_PAYMENT_DASHBOARD_NOTIFICATION.name()).build()
            ).build();

            handler.handle(params);

            verifyNoInteractions(dashboardApiClient);
        }
    }
}
