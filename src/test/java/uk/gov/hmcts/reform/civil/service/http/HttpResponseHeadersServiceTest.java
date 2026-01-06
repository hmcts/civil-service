package uk.gov.hmcts.reform.civil.service.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.model.callback.TaskCompletionSubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.model.taskmanagement.ClientContext;
import uk.gov.hmcts.reform.civil.model.taskmanagement.ClientContextWrapper;
import uk.gov.hmcts.reform.civil.model.taskmanagement.Task;
import uk.gov.hmcts.reform.civil.model.taskmanagement.UserTask;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.HashMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SuppressWarnings("unchecked")
class HttpResponseHeadersServiceTest {

    private ObjectMapper mapper;
    private HttpResponseHeadersService responseHeadersService;

    @BeforeEach
    void setupTests() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        responseHeadersService = new HttpResponseHeadersService(mapper);
    }

    @Test
    void shouldAddClientContextHeaderWhenCallbackResponseIsCorrectType() {
        HttpServletResponse mockedResponse = mock(HttpServletResponse.class);
        String expectedEncodedContext = "ZXhwbGVjaXRseSBlbmNvZGVkIGNsaWVudCBjb250ZXh0";
        HttpResponseHeadersService headerSpyService = spy(responseHeadersService);
        when(headerSpyService.encodeClientContext(any(ClientContextWrapper.class)))
            .thenReturn(expectedEncodedContext);

        TaskCompletionSubmittedCallbackResponse callbackResponse = TaskCompletionSubmittedCallbackResponse.builder()
            .clientContext(buildClientContext().getClientContext())
            .build();

        headerSpyService.addClientContextHeader(callbackResponse, mockedResponse);

        verify(mockedResponse).addHeader("client-context", expectedEncodedContext);
    }

    @Test
    void shouldNotAddClientContextHeaderWhenCallbackResponseIsIncorrectType() {
        HttpServletResponse mockedResponse = mock(HttpServletResponse.class);
        SubmittedCallbackResponse callbackResponse = SubmittedCallbackResponse.builder().build();

        responseHeadersService.addClientContextHeader(callbackResponse, mockedResponse);

        verify(mockedResponse, never()).addHeader(anyString(), anyString());
    }

    @Test
    void encodeClientContext_shouldReturnEncodedString() throws Exception {
        ClientContextWrapper clientContext = buildClientContext();

        String actualEncoded = responseHeadersService.encodeClientContext(clientContext);

        assertNotNull(actualEncoded);
        assertThat(decodeClientContext(actualEncoded))
            .extracting("client_context")
            .extracting("user_task")
            .extracting("task_data")
            .extracting("task_title")
            .isEqualTo("My Task");
    }

    private ClientContextWrapper buildClientContext() {
        return ClientContextWrapper.builder()
            .clientContext(ClientContext.builder()
                               .userTask(
                                   UserTask.builder()
                                       .taskData(Task.builder().taskTitle("My Task").build())
                                       .build())
                               .build())
            .build();
    }

    private HashMap<String, Object> decodeClientContext(String encodedClientContext) throws Exception {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedClientContext);
        String decodedString = new String(decodedBytes);
        return mapper.readValue(decodedString, HashMap.class);
    }
}
