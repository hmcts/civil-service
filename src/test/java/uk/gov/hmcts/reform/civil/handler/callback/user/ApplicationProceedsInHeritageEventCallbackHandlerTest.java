package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.NON_LIVE_STATES;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICATION_PROCEEDS_IN_HERITAGE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    ApplicationProceedsInHeritageEventCallbackHandler.class, JacksonAutoConfiguration.class, Time.class
})
class ApplicationProceedsInHeritageEventCallbackHandlerTest  extends BaseCallbackHandlerTest {

    @MockBean
    private Time time;
    @MockBean
    GaForLipService gaForLipService;
    @MockBean
    private DocUploadDashboardNotificationService dashboardNotificationService;

    @Autowired
    private ApplicationProceedsInHeritageEventCallbackHandler handler;

    @Nested
    class AboutToSubmitCallback {

        private LocalDateTime localDateTime;

        @BeforeEach
        void setup() {
            localDateTime = LocalDateTime.now();
            when(time.now()).thenReturn(localDateTime);
            when(gaForLipService.isGaForLip(any())).thenReturn(false);
        }

        @ParameterizedTest(name = "The application is in {0} state")
        @EnumSource(value = CaseState.class)
        void shouldRespondWithStateChangedWhenApplicationIsLive(CaseState state) {
            CaseData caseData = CaseDataBuilder.builder()
                .ccdCaseReference(1234L)
                .ccdState(state).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            if (!NON_LIVE_STATES.contains(state)) {
                assertThat(response.getErrors()).isNull();
                assertThat(response.getState()).isEqualTo(PROCEEDS_IN_HERITAGE.toString());
                assertThat(response.getData()).extracting("businessProcess").extracting("status").isEqualTo("FINISHED");
                assertThat(response.getData()).extracting("businessProcess").extracting("camundaEvent").isEqualTo(
                    "APPLICATION_PROCEEDS_IN_HERITAGE");
                assertThat(response.getData()).containsEntry(
                    "applicationTakenOfflineDate",
                    localDateTime.format(ISO_DATE_TIME)
                );
            } else {
                assertThat(response).isEqualTo(AboutToStartOrSubmitCallbackResponse.builder().build());
            }
        }

        @Test
        void shouldThrowNotificationTwiceWhenCaseIsLipVsLip() {
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            when(gaForLipService.isLipResp(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .ccdCaseReference(1234L)
                .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(dashboardNotificationService, times(2))
                .createOfflineResponseDashboardNotification(any(), any(), anyString());
            assertThat(response).isNotNull();
        }

        @Test
        void shouldNotSendAnyDashboardNotificationsWhenLRvsLR() {
            when(gaForLipService.isGaForLip(any())).thenReturn(false);
            when(gaForLipService.isLipApp(any())).thenReturn(false);
            when(gaForLipService.isLipResp(any())).thenReturn(false);
            CaseData caseData = CaseDataBuilder.builder()
                .ccdCaseReference(1234L)
                .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verifyNoInteractions(dashboardNotificationService);

            assertThat(response).isNotNull();
        }

        @Test
        void shouldThrowNotificationApplicationWhenItisLipCaseAndLipApplicant() {
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            when(gaForLipService.isLipResp(any())).thenReturn(false);
            CaseData caseData = CaseDataBuilder.builder()
                .ccdCaseReference(1234L)
                .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(dashboardNotificationService).createOfflineResponseDashboardNotification(any(), any(), anyString());
            assertThat(response).isNotNull();
        }

        @Test
        void shouldThrowNotificationOnlyOnceToLipApplicant() {
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            when(gaForLipService.isLipResp(any())).thenReturn(false);
            CaseData caseData = CaseDataBuilder.builder()
                .ccdCaseReference(1234L)
                .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(dashboardNotificationService).createOfflineResponseDashboardNotification(any(), any(), anyString());
            assertThat(response).isNotNull();
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(APPLICATION_PROCEEDS_IN_HERITAGE);
    }
}
