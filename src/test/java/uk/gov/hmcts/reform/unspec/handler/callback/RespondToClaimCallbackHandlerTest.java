package uk.gov.hmcts.reform.unspec.handler.callback;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.enums.DefendantResponseType;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.unspec.validation.DateOfBirthValidator;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.enums.DefendantResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.unspec.handler.callback.RespondToClaimCallbackHandler.CLAIMANT_RESPONSE_DEADLINE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RespondToClaimCallbackHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    DateOfBirthValidator.class
})
class RespondToClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

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

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {

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
        void shouldSetDefendantResponseBusinessProcessToReady_whenResponseIsFullDefence() {
            Map<String, Object> data = new HashMap<>(Map.of(
                "respondent1ClaimResponseType", FULL_DEFENCE
            ));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(callbackParamsOf(data, CallbackType.ABOUT_TO_SUBMIT));

            //TODO: uncomment when CMC-794 is played
            //assertThat(response.getData()).extracting("businessProcess").extracting("status").isEqualTo(READY);
            assertThat(response.getData()).extracting("businessProcess").extracting("activityId").isEqualTo(
                "DefendantResponseHandling");
            assertThat(response.getData()).extracting("businessProcess").extracting("processInstanceId").isNull();
        }

        @ParameterizedTest
        @EnumSource(
            value = DefendantResponseType.class,
            names = {"FULL_ADMISSION", "PART_ADMISSION", "COUNTER_CLAIM"})
        void shouldSetCaseHandedOfflineBusinessProcessToReady_whenResponseIsNotFullDefence(
            DefendantResponseType defendantResponse) {
            Map<String, Object> data = new HashMap<>(Map.of(
                "respondent1ClaimResponseType", defendantResponse
            ));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(callbackParamsOf(data, CallbackType.ABOUT_TO_SUBMIT));

            //TODO: uncomment when CMC-794 is played
            //assertThat(response.getData()).extracting("businessProcess").extracting("status").isEqualTo(READY);
            assertThat(response.getData()).extracting("businessProcess").extracting("activityId").isEqualTo(
                "CaseHandedOfflineHandling");
            assertThat(response.getData()).extracting("businessProcess").extracting("processInstanceId").isNull();
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
