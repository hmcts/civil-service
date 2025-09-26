package uk.gov.hmcts.reform.civil.model.callback;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.model.taskmanagement.ClientContext;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class TaskCompletionSubmittedCallbackResponse implements CallbackResponse {

    @JsonProperty("confirmation_header")
    private String confirmationHeader;

    @JsonProperty("confirmation_body")
    private String confirmationBody;

    @JsonIgnoreProperties
    private ClientContext clientContext;
}
