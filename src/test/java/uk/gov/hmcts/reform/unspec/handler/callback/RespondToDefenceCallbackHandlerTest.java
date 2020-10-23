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
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.unspec.service.BusinessProcessService;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.YES;

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
            when(businessProcessService.updateBusinessProcess(any(), any())).thenReturn(CaseData.builder().build());
            clearInvocations(businessProcessService);
        }

        @Test
        void shouldUpdateBusinessProcess_whenAtFullDefenceState() {
            CaseData caseData = CaseDataBuilder.builder().atStateFullDefence().build();

            handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

            verify(businessProcessService).updateBusinessProcess(caseData, CLAIMANT_RESPONSE);
        }

        @Test
        void shouldNotUpdateBusinessProcess_whenNotAtFullDefenceState() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondedToClaim().build();

            handler.handle(callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

            verifyNoInteractions(businessProcessService);
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedResponse_whenApplicantIsProceedingWithClaim() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateFullDefence()
                .applicant1ProceedWithClaim(YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

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
            CaseData caseData = CaseDataBuilder.builder()
                .atStateFullDefence()
                .applicant1ProceedWithClaim(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualToComparingFieldByField(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format("# You've decided not to proceed with the claim%n## Claim number: TBC"))
                    .confirmationBody("CONTENT TBC")
                    .build());
        }
    }
}
