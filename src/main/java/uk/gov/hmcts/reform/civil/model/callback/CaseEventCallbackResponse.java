package uk.gov.hmcts.reform.civil.model.callback;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.Event;

import java.util.Map;

@Getter
@Builder
public class CaseEventCallbackResponse implements CallbackResponse {

    private final Map<String, Object> data;

    @JsonProperty("case_event")
    private final Event caseEvent;
}
