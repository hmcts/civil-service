package uk.gov.hmcts.reform.civil.model.taskmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder (toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ClientContextWrapper {

    @JsonProperty("client_context")
    private ClientContext clientContext;

}
