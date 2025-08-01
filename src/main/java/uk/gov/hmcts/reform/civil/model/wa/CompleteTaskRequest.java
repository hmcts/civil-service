package uk.gov.hmcts.reform.civil.model.wa;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(
    name = "CompleteTaskRequest",
    description = "Allows specifying certain completion options"
)
@EqualsAndHashCode
@ToString
public class CompleteTaskRequest {

    @Schema(name = "completion_options")
    @JsonProperty("completion_options")
    private final CompletionOptions completionOptions;

    @JsonCreator
    public CompleteTaskRequest(CompletionOptions completionOptions) {
        this.completionOptions = completionOptions;
    }

    public CompletionOptions getCompletionOptions() {
        return completionOptions;
    }
}
