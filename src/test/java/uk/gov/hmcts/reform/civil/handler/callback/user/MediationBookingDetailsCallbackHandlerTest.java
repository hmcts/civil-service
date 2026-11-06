package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationAppointmentType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MEDIATION_BOOKING_DETAILS;

@ExtendWith(MockitoExtension.class)
class MediationBookingDetailsCallbackHandlerTest extends BaseCallbackHandlerTest {

    private MediationBookingDetailsCallbackHandler handler;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new MediationBookingDetailsCallbackHandler(objectMapper);
    }

    @Test
    void shouldHandleMediationBookingDetailsEvent() {
        assertThat(handler.handledEvents()).containsOnly(MEDIATION_BOOKING_DETAILS);
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldOpenTheEventWithoutErrors() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNullOrEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRetainSelectedAppointmentType() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            caseData.setMediationAppointmentType(MediationAppointmentType.AUTO);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNullOrEmpty();
            assertThat(response.getData()).containsEntry("mediationAppointmentType", "AUTO");
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnConfirmationScreen() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response.getConfirmationHeader())
                .isEqualTo("# Mediation appointments details recorded");
            assertThat(response.getConfirmationBody())
                .contains("What happens next")
                .contains("saved on the Summary tab");
        }
    }
}
