package uk.gov.hmcts.reform.civil.model.taskmanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class GetTasksResponse {

    private List<Task> tasks;
    private long totalRecords;

}
