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
public class ClientContext {

    @JsonProperty("user_task")
    private UserTask userTask;
    @JsonProperty("user_language")
    private UserLanguage userLanguage;
}
