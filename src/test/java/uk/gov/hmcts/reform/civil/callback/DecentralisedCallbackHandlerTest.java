package uk.gov.hmcts.reform.civil.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.callback.TaskCompletionSubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.model.taskmanagement.ClientContext;
import uk.gov.hmcts.reform.civil.model.taskmanagement.Task;
import uk.gov.hmcts.reform.civil.model.taskmanagement.UserTask;
import uk.gov.hmcts.reform.civil.service.http.HttpResponseHeadersService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;

@ExtendWith(MockitoExtension.class)
class DecentralisedCallbackHandlerTest {

    private static final String AUTHORISATION = "Bearer user-token";

    @Mock
    private CallbackHandlerFactory callbackHandlerFactory;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    private final HttpResponseHeadersService responseHeadersService =
        new HttpResponseHeadersService(new ObjectMapper().registerModule(new JavaTimeModule()));

    private DecentralisedCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DecentralisedCallbackHandler(callbackHandlerFactory, caseDetailsConverter, responseHeadersService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/cases/callbacks/about-to-submit",
        "/cases/callbacks/submitted",
        "/cases/callbacks/version/V_1/about-to-submit",
        "/cases/callbacks/version/V_2/about-to-submit",
        "/cases/callbacks/mid/page-id"
    })
    void handlesEveryPathCallbackControllerMaps(String path) {
        assertThat(handler.handles(path)).isTrue();
    }

    @Test
    void doesNotHandleOtherPaths() {
        assertThat(handler.handles("/cases/other/about-to-submit")).isFalse();
    }

    @Test
    void dispatchesVersionedAboutToSubmitAsCallbackControllerWould() {
        CallbackResponse expected = AboutToStartOrSubmitCallbackResponse.builder().build();
        when(callbackHandlerFactory.dispatch(any())).thenReturn(expected);

        Object response = handler.handle(
            "/cases/callbacks/version/V_1/about-to-submit", request(CASE_TYPE, "CREATE_CLAIM"), headers()
        );

        CallbackParams params = dispatchedParams();
        assertThat(response).isSameAs(expected);
        assertThat(params.getType()).isEqualTo(ABOUT_TO_SUBMIT);
        assertThat(params.getVersion()).isEqualTo(V_1);
        assertThat(params.getPageId()).isNull();
        assertThat(params.getParams()).containsEntry(BEARER_TOKEN, AUTHORISATION);
        assertThat(params.getRequest().getEventId()).isEqualTo("CREATE_CLAIM");
        assertThat(params.isCivilCaseType()).isTrue();
        assertThat(params.isGeneralApplicationCaseType()).isFalse();
    }

    @Test
    void dispatchesGeneralApplicationCallbacksAsGeneralApplicationCaseType() {
        when(callbackHandlerFactory.dispatch(any())).thenReturn(AboutToStartOrSubmitCallbackResponse.builder().build());

        handler.handle(
            "/cases/callbacks/about-to-submit", request(GENERALAPPLICATION_CASE_TYPE, "RESPOND_TO_APPLICATION"), headers()
        );

        CallbackParams params = dispatchedParams();
        assertThat(params.getVersion()).isNull();
        assertThat(params.isGeneralApplicationCaseType()).isTrue();
        assertThat(params.isCivilCaseType()).isFalse();
    }

    @Test
    void returnsTheClientContextHeaderCallbackControllerWouldReturn() {
        TaskCompletionSubmittedCallbackResponse submitted = taskCompletion();
        when(callbackHandlerFactory.dispatch(any())).thenReturn(submitted);

        Object response = handler.handle("/cases/callbacks/submitted", request(CASE_TYPE, "SEND_AND_REPLY"), headers());

        assertThat(dispatchedParams().getType()).isEqualTo(SUBMITTED);
        assertThat(response).isInstanceOf(ResponseEntity.class);
        ResponseEntity<?> entity = (ResponseEntity<?>) response;
        assertThat(entity.getBody()).isSameAs(submitted);
        assertThat(entity.getHeaders().getFirst("client-context"))
            .isEqualTo(responseHeadersService.clientContextHeader(submitted).orElseThrow());
    }

    @Test
    void returnsTheBareResponseWhenTheCallbackNamesNoTask() {
        CallbackResponse submitted = SubmittedCallbackResponse.builder().confirmationHeader("# Sent").build();
        when(callbackHandlerFactory.dispatch(any())).thenReturn(submitted);

        Object response = handler.handle("/cases/callbacks/submitted", request(CASE_TYPE, "SEND_AND_REPLY"), headers());

        assertThat(response).isSameAs(submitted);
    }

    private CallbackParams dispatchedParams() {
        ArgumentCaptor<CallbackParams> captor = ArgumentCaptor.forClass(CallbackParams.class);
        verify(callbackHandlerFactory).dispatch(captor.capture());
        return captor.getValue();
    }

    private static CallbackRequest request(String caseTypeId, String eventId) {
        return CallbackRequest.builder()
            .eventId(eventId)
            .caseDetails(CaseDetails.builder().id(1234L).caseTypeId(caseTypeId).data(Map.of()).build())
            .build();
    }

    private static HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, AUTHORISATION);
        headers.set("ServiceAuthorization", "s2s-token");
        return headers;
    }

    private static TaskCompletionSubmittedCallbackResponse taskCompletion() {
        Task task = new Task().setId("task-1");
        return new TaskCompletionSubmittedCallbackResponse()
            .setConfirmationHeader("# Your reply has been sent")
            .setClientContext(new ClientContext().setUserTask(new UserTask(task, true)));
    }
}
