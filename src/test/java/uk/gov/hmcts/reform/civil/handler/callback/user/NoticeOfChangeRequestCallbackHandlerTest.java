package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.cas.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

@ExtendWith(MockitoExtension.class)
public class NoticeOfChangeRequestCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private NoticeOfChangeRequestCallbackHandler handler;

    @Mock
    private CaseAssignmentApi caseAssignmentApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private static final String invalidStateNoC = "Invalid case state for NoC";

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldReturnError_whenNoCCaseStateProceedsInHeritageSystem() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAfterClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors().contains(invalidStateNoC)).isTrue();
        }

        @Test
        void shouldReturnError_whenNoCCaseStateCaseDismissed() {
            CaseData caseData = CaseDataBuilder.builder().discontinueClaim().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors().contains(invalidStateNoC)).isTrue();
        }

        @Test
        void shouldReturnError_whenNoCCaseStatePendingCaseIssued() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors().contains(invalidStateNoC)).isTrue();
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenNoCCaseStateIsValid() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            when(caseAssignmentApi.checkNocApproval(params.getParams().get(BEARER_TOKEN).toString(),
                                                    authTokenGenerator.generate(),
                                                    params.getRequest())).thenReturn(SubmittedCallbackResponse.builder()
                                                                                                     .build());
            handler.handle(params);
            verify(caseAssignmentApi).checkNocApproval(params.getParams().get(BEARER_TOKEN).toString(),
                                                       authTokenGenerator.generate(),
                                                       params.getRequest());
        }
    }
}
