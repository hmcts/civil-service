package uk.gov.hmcts.reform.civil.model.wa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder (toBuilder = true)
@AllArgsConstructor
public class ClientContextWrapper {

    @JsonProperty("client_context")
    private ClientContext clientContext;

}
