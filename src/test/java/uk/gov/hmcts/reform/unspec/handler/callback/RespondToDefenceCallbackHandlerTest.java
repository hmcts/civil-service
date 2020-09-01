package uk.gov.hmcts.reform.unspec.handler.callback;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RespondToDefenceCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class RespondToDefenceCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private RespondToDefenceCallbackHandler handler;

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
