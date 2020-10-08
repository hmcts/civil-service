package uk.gov.hmcts.reform.unspec.handler.callback;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.enums.DefendantResponseType;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RespondToDefenceCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class RespondToDefenceCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private RespondToDefenceCallbackHandler handler;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateRespondedToClaim().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldSetCaseTransferredToLocalCourtBusinessProcessToReady_whenFullDefenceAndProceedingWithClaim() {
            Map<String, Object> data = new HashMap<>(Map.of(
                "respondent1ClaimResponseType", "FULL_DEFENCE",
                "applicant1ProceedWithClaim", "Yes"
            ));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(callbackParamsOf(data, CallbackType.ABOUT_TO_SUBMIT));

            //TODO: uncomment when CMC-794 is played
            //assertThat(response.getData()).extracting("businessProcess").extracting("status").isEqualTo(READY);
            assertThat(response.getData()).extracting("businessProcess").extracting("activityId").isEqualTo(
                "CaseTransferredToLocalCourtHandling");
            assertThat(response.getData()).extracting("businessProcess").extracting("processInstanceId").isNull();
        }

        @ParameterizedTest
        @EnumSource(
            value = DefendantResponseType.class,
            names = {"FULL_ADMISSION", "PART_ADMISSION", "COUNTER_CLAIM"})
        void shouldNotSetBusinessProcess_whenNotFullDefence(DefendantResponseType responseType) {
            Map<String, Object> data = new HashMap<>(Map.of(
                "respondent1ClaimResponseType", responseType,
                "applicant1ProceedWithClaim", "Yes"
            ));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(callbackParamsOf(data, CallbackType.ABOUT_TO_SUBMIT));

            assertThat(response.getData()).doesNotContainKey("businessProcess");
        }

        @ParameterizedTest
        @EnumSource(
            value = DefendantResponseType.class,
            names = {"FULL_DEFENCE", "FULL_ADMISSION", "PART_ADMISSION", "COUNTER_CLAIM"})
        void shouldNotSetBusinessProcess_whenNotProceedingWithClaim(DefendantResponseType responseType) {
            Map<String, Object> data = new HashMap<>(Map.of(
                "respondent1ClaimResponseType", responseType,
                "applicant1ProceedWithClaim", "No"
            ));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(callbackParamsOf(data, CallbackType.ABOUT_TO_SUBMIT));

            assertThat(response.getData()).doesNotContainKey("businessProcess");
        }
    }

    @Nested
    class SubmittedCallback {
        public static final String APPLICANT_1_PROCEEDING = "applicant1ProceedWithClaim";

        @Test
        void shouldReturnExpectedResponse_whenApplicantIsProceedingWithClaim() {
            Map<String, Object> data = new HashMap<>();
            data.put(APPLICANT_1_PROCEEDING, YesOrNo.YES);

            CallbackParams params = callbackParamsOf(data, CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualToComparingFieldByField(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# You've decided to proceed with the claim%n## Claim number: TBC"))
                    .confirmationBody(format(
                        "<br />We'll review the case. We'll contact you to tell you what to do next.%n%n"
                            + "[Download directions questionnaire](http://www.google.com)"))
                    .build());
        }

        @Test
        void shouldReturnExpectedResponse_whenApplicantIsNotProceedingWithClaim() {
            Map<String, Object> data = new HashMap<>();
            data.put(APPLICANT_1_PROCEEDING, YesOrNo.NO);

            CallbackParams params = callbackParamsOf(data, CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualToComparingFieldByField(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# You've decided not to proceed with the claim%n## Claim number: TBC"))
                    .confirmationBody("CONTENT TBC")
                    .build());
        }
    }
}
