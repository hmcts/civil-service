package uk.gov.hmcts.reform.civil.handler.callback.user;

import feign.FeignException;
import feign.Request;
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

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
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

    private static final String INVALID_STATE_NOC = "Invalid case state for NoC";

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldReturnError_whenNoCCaseStateProceedsInHeritageSystem() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAfterClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors().contains(INVALID_STATE_NOC)).isTrue();
        }

        @Test
        void shouldReturnError_whenNoCCaseStateCaseDismissed() {
            CaseData caseData = CaseDataBuilder.builder().discontinueClaim().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors().contains(INVALID_STATE_NOC)).isTrue();
        }

        @Test
        void shouldReturnError_whenNoCCaseStatePendingCaseIssued() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors().contains(INVALID_STATE_NOC)).isTrue();
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

        @Test
        void shouldReturnConfirmation_whenCheckNocApprovalFailsWithBadGateway() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            FeignException badGateway = new FeignException.BadGateway(
                "Bad Gateway",
                Request.create(Request.HttpMethod.POST, "/noc/check-noc-approval", Map.of(), null, StandardCharsets.UTF_8, null),
                "upstream unavailable".getBytes(StandardCharsets.UTF_8),
                Map.of()
            );

            when(caseAssignmentApi.checkNocApproval(params.getParams().get(BEARER_TOKEN).toString(),
                                                    authTokenGenerator.generate(),
                                                    params.getRequest())).thenThrow(badGateway);

            SubmittedCallbackResponse response = assertDoesNotThrow(
                () -> (SubmittedCallbackResponse) handler.handle(params)
            );

            assertThat(response.getConfirmationHeader()).isEqualTo("# Notice of change request submitted");
            assertThat(response.getConfirmationBody()).contains("We could not confirm approval");
        }

        @Test
        void shouldReturnConfirmation_whenCheckNocApprovalFailsWithBlankResponseBody() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            FeignException feignException = mock(FeignException.class);
            when(feignException.status()).thenReturn(502);
            when(feignException.contentUTF8()).thenReturn(" ");

            when(caseAssignmentApi.checkNocApproval(params.getParams().get(BEARER_TOKEN).toString(),
                                                    authTokenGenerator.generate(),
                                                    params.getRequest())).thenThrow(feignException);

            SubmittedCallbackResponse response = assertDoesNotThrow(
                () -> (SubmittedCallbackResponse) handler.handle(params)
            );

            assertThat(response.getConfirmationHeader()).isEqualTo("# Notice of change request submitted");
            assertThat(response.getConfirmationBody()).contains("We could not confirm approval");
        }

        @Test
        void shouldReturnConfirmation_whenCheckNocApprovalFailsWithNullResponseBody() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            FeignException feignException = mock(FeignException.class);
            when(feignException.status()).thenReturn(502);
            when(feignException.contentUTF8()).thenReturn(null);

            when(caseAssignmentApi.checkNocApproval(params.getParams().get(BEARER_TOKEN).toString(),
                                                    authTokenGenerator.generate(),
                                                    params.getRequest())).thenThrow(feignException);

            SubmittedCallbackResponse response = assertDoesNotThrow(
                () -> (SubmittedCallbackResponse) handler.handle(params)
            );

            assertThat(response.getConfirmationHeader()).isEqualTo("# Notice of change request submitted");
            assertThat(response.getConfirmationBody()).contains("We could not confirm approval");
        }
    }
}
