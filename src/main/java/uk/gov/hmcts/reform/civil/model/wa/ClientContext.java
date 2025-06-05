package uk.gov.hmcts.reform.civil.model.wa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientContext {

    @JsonProperty("user_task")
    private UserTask userTask;
}
