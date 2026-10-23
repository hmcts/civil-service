package uk.gov.hmcts.reform.civil.service.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.model.callback.TaskCompletionSubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.model.taskmanagement.ClientContext;
import uk.gov.hmcts.reform.civil.model.taskmanagement.ClientContextWrapper;
import uk.gov.hmcts.reform.civil.model.taskmanagement.Task;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HttpResponseHeadersService {

    public static final String CLIENT_CONTEXT = "client-context";

    private final ObjectMapper mapper;

    public void addClientContextHeader(CallbackResponse callbackResponse, HttpServletResponse response) {
        clientContextHeader(callbackResponse)
            .ifPresent(encodedClientContext -> response.addHeader(CLIENT_CONTEXT, encodedClientContext));
    }

    /**
     * The client-context header that asks ExUI to complete the WA task a submitted callback names, if it names one.
     */
    public Optional<String> clientContextHeader(CallbackResponse callbackResponse) {
        try {
            if (callbackResponse instanceof TaskCompletionSubmittedCallbackResponse submittedResponse) {
                ClientContext clientContext = submittedResponse.getClientContext();
                Task taskToComplete = clientContext.getUserTask().getTaskData();
                log.info(
                    "Setting client context to complete the [{}] WA task: [{}]",
                    taskToComplete.getTaskTitle(),
                    taskToComplete.getId()
                );
                return Optional.ofNullable(encodeClientContext(new ClientContextWrapper().setClientContext(clientContext)));
            }
        } catch (Exception e) {
            log.error("There was a problem adding the client context header to callback response: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public String encodeClientContext(ClientContextWrapper clientContext) {
        try {
            String jsonString = mapper.writeValueAsString(clientContext);
            byte[] encodedBytes = Base64.getEncoder().encode(jsonString.getBytes());
            return new String(encodedBytes);

        } catch (Exception ex) {
            log.error("Exception while serializing client context: {}", ex.getMessage());
            return null;
        }
    }
}
