package uk.gov.hmcts.reform.civil.model.taskmanagement;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@Builder
public class GetTasksResponse {

    private List<Task> tasks;
    private long totalRecords;

}
