package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DELETE_APPLICATION_PAYMENT_DASHBOARD_NOTIFICATION;

@ExtendWith(MockitoExtension.class)
public class DeleteApplicationPaymentDashboardNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DashboardApiClient dashboardApiClient;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private GaForLipService gaForLipService;
    private DeleteApplicationPaymentDashboardNotificationHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DeleteApplicationPaymentDashboardNotificationHandler(
            dashboardApiClient,
            featureToggleService,
            gaForLipService,
            objectMapper
        );
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(DELETE_APPLICATION_PAYMENT_DASHBOARD_NOTIFICATION);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRemoveNotification_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipAppGa(any(GeneralApplicationCaseData.class))).thenReturn(true);
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
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(DELETE_APPLICATION_PAYMENT_DASHBOARD_NOTIFICATION.name()).build()
            ).build();

            handler.handle(params);

            verifyNoInteractions(dashboardApiClient);
        }

        @Test
        void shouldRemoveNotificationWhenOnlyGaCaseDataProvided() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            GeneralApplicationCaseData gaCaseData = toGaCaseData(caseData);
            when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipAppGa(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);

            CallbackParams params = gaCallbackParamsOf(gaCaseData, ABOUT_TO_SUBMIT);

            handler.handle(params);

            verify(dashboardApiClient).deleteTemplateNotificationsForCaseIdentifierAndRole(
                String.valueOf(gaCaseData.getCcdCaseReference()),
                "APPLICANT",
                "Notice.AAA6.GeneralApps.ApplicationFeeRequired.Applicant",
                "BEARER_TOKEN"
            );
        }
    }
}
