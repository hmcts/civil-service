package uk.gov.hmcts.reform.unspec.handler.callback;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.unspec.service.BusinessProcessService;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CLAIMANT_RESPONSE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RespondToDefenceCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class
})
class RespondToDefenceCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private BusinessProcessService businessProcessService;

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

        @BeforeEach
        void setup() {
            when(businessProcessService.updateBusinessProcess(any(), any())).thenReturn(List.of());
            clearInvocations(businessProcessService);
        }

        @Test
        void shouldUpdateBusinessProcess_whenAtFullDefenceState() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateFullDefence().build();

            handler.handle(callbackParamsOf(caseDetails.getData(), CallbackType.ABOUT_TO_SUBMIT));

            verify(businessProcessService).updateBusinessProcess(caseDetails.getData(), CLAIMANT_RESPONSE);
        }

        @Test
        void shouldNotUpdateBusinessProcess_whenNotAtFullDefenceState() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateRespondedToClaim().build();

            handler.handle(callbackParamsOf(caseDetails.getData(), CallbackType.ABOUT_TO_SUBMIT));

            verifyNoInteractions(businessProcessService);
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
