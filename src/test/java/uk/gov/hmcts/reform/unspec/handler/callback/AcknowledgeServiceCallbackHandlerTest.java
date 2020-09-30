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
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.unspec.service.WorkingDayIndicator;
import uk.gov.hmcts.reform.unspec.validation.DateOfBirthValidator;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator.MID_NIGHT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    AcknowledgeServiceCallbackHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    DateOfBirthValidator.class
})
class AcknowledgeServiceCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private WorkingDayIndicator workingDayIndicator;

    @Autowired
    private AcknowledgeServiceCallbackHandler handler;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateServiceConfirmed().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class MidEventCallback {

        @ParameterizedTest
        @ValueSource(strings = {"individualDateOfBirth", "soleTraderDateOfBirth"})
        void shouldReturnError_whenDateOfBirthIsInTheFuture(String dateOfBirthField) {
            Map<String, Object> data = new HashMap<>();
            data.put("respondent1", Map.of(dateOfBirthField, "2030-01-01"));

            CallbackParams params = callbackParamsOf(data, CallbackType.MID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).containsExactly("The date entered cannot be in the future");
        }

        @ParameterizedTest
        @ValueSource(strings = {"individualDateOfBirth", "soleTraderDateOfBirth"})
        void shouldReturnNoError_whenDateOfBirthIsInThePast(String dateOfBirthField) {
            Map<String, Object> data = new HashMap<>();
            data.put("respondent1", Map.of(dateOfBirthField, "2000-01-01"));

            CallbackParams params = callbackParamsOf(data, CallbackType.MID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData()).isEqualTo(data);
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(workingDayIndicator.getNextWorkingDay(any())).thenReturn(now().plusDays(14));
        }

        @Test
        void shouldSetNewResponseDeadline_whenInvoked() {
            Map<String, Object> data = new HashMap<>();
            LocalDateTime responseDeadline = now().atTime(MID_NIGHT);
            data.put("respondentSolicitor1ResponseDeadline", responseDeadline);

            CallbackParams params = callbackParamsOf(data, CallbackType.ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData()).isEqualTo(Map.of(
                "respondentSolicitor1ResponseDeadline",
                responseDeadline.plusDays(14)
            ));
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedResponse_whenInvoked() {
            Map<String, Object> data = new HashMap<>();
            data.put("respondentSolicitor1ResponseDeadline", "2030-01-01T16:00:00");

            CallbackParams params = callbackParamsOf(data, CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualToComparingFieldByField(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# You've acknowledged service")
                    .confirmationBody("<br />You need to respond before 4pm on 1 January 2030."
                                          + "\n\n[Download the Acknowledgement of Service form](http://www.google.com)")
                    .build());
        }
    }
}
