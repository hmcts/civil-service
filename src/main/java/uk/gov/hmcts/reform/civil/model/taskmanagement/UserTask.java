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
public class UserTask {

    @JsonProperty("task_data")
    private Task taskData;
    @JsonProperty("complete_task")
    private boolean completeTask;
}
