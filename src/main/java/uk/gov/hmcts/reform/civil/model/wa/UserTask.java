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
public class UserTask {

    @JsonProperty("task_data")
    private TaskData taskData;
    @JsonProperty("complete_task")
    private boolean completeTask;
}
