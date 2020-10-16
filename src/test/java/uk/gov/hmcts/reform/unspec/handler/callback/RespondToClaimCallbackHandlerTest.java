package uk.gov.hmcts.reform.unspec.handler.callback;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.UnavailableDate;
import uk.gov.hmcts.reform.unspec.model.dq.Hearing;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.unspec.service.BusinessProcessService;
import uk.gov.hmcts.reform.unspec.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.unspec.validation.UnavailableDateValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.unspec.handler.callback.RespondToClaimCallbackHandler.CLAIMANT_RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.unspec.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RespondToClaimCallbackHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    DateOfBirthValidator.class,
    UnavailableDateValidator.class,
    CaseDetailsConverter.class
})
class RespondToClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private BusinessProcessService businessProcessService;

    @Autowired
    private RespondToClaimCallbackHandler handler;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateServiceAcknowledge().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class MidEventConfirmDetailsCallback {

        @ParameterizedTest
        @ValueSource(strings = {"individualDateOfBirth", "soleTraderDateOfBirth"})
        void shouldReturnError_whenDateOfBirthIsInTheFuture(String dateOfBirthField) {
            Map<String, Object> data = new HashMap<>();
            data.put("respondent1", Map.of(dateOfBirthField, "2030-01-01"));

            CallbackParams params = callbackParamsOf(data, MID, "confirm-details");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @ParameterizedTest
        @ValueSource(strings = {"individualDateOfBirth", "soleTraderDateOfBirth"})
        void shouldReturnNoError_whenDateOfBirthIsInThePast(String dateOfBirthField) {
            Map<String, Object> data = new HashMap<>();
            data.put("respondent1", Map.of(dateOfBirthField, "2000-01-01"));

            CallbackParams params = callbackParamsOf(data, MID, "confirm-details");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventCallbackValidateUnavailableDates {

        @Test
        void shouldReturnError_whenUnavailableDateIsMoreThanOneYearInFuture() {
            Map<String, Object> data = new HashMap<>();
            data.put("respondent1DQHearing", Hearing.builder()
                .unavailableDates(wrapElements(UnavailableDate.builder()
                    .date(LocalDate.now().plusYears(5))
                    .build()))
                .build());

            CallbackParams params = callbackParamsOf(data, MID, "validate-unavailable-dates");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors())
                .containsExactly("The date cannot be in the past and must not be more than a year in the future");
        }

        @Test
        void shouldReturnError_whenUnavailableDateIsInPast() {
            Map<String, Object> data = new HashMap<>();
            data.put("respondent1DQHearing", Hearing.builder()
                .unavailableDates(wrapElements(UnavailableDate.builder()
                    .date(LocalDate.now().minusYears(5))
                    .build()))
                .build());

            CallbackParams params = callbackParamsOf(data, MID, "validate-unavailable-dates");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors())
                .containsExactly("The date cannot be in the past and must not be more than a year in the future");
        }

        @Test
        void shouldReturnNoError_whenUnavailableDateIsValid() {
            Map<String, Object> data = new HashMap<>();
            data.put("respondent1DQHearing", Hearing.builder()
                .unavailableDates(wrapElements(UnavailableDate.builder()
                    .date(LocalDate.now().plusDays(5))
                    .build()))
                .build());

            CallbackParams params = callbackParamsOf(data, MID, "validate-unavailable-dates");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoError_whenNoUnavailableDate() {
            Map<String, Object> data = new HashMap<>();
            data.put("respondent1DQHearing", Hearing.builder().build());

            CallbackParams params = callbackParamsOf(data, MID, "validate-unavailable-dates");

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        public void setup() {
            when(businessProcessService.updateBusinessProcess(any(), any())).thenReturn(List.of());
        }

        @Test
        void shouldSetClaimantResponseDeadline_whenInvoked() {
            Map<String, Object> data = new HashMap<>();
            LocalDateTime claimantResponseDeadline = now().atTime(16, 0);

            CallbackParams params = callbackParamsOf(data, CallbackType.ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData()).containsEntry(CLAIMANT_RESPONSE_DEADLINE, claimantResponseDeadline);
        }

        @Test
        void shouldUpdateBusinessProcess_whenInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateRespondedToClaim().build();

            handler.handle(callbackParamsOf(caseDetails.getData(), CallbackType.ABOUT_TO_SUBMIT));

            verify(businessProcessService).updateBusinessProcess(caseDetails.getData(), DEFENDANT_RESPONSE);
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedResponse_whenInvoked() {
            Map<String, Object> data = new HashMap<>();
            data.put(CLAIMANT_RESPONSE_DEADLINE, "2030-01-01T16:00:00");

            CallbackParams params = callbackParamsOf(data, CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualToComparingFieldByField(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# You've submitted your response%n## Claim number: TBC"))
                    .confirmationBody("<br />The claimant has until 1 January 2030 to proceed. "
                                          + "We will let you know when they respond.")
                    .build());
        }
    }
}
