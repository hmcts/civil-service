package uk.gov.hmcts.reform.civil.model.wa;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode
@ToString
public class GetTasksResponse<T extends TaskData> {

    private final List<T> tasks;

    private final long totalRecords;

    public GetTasksResponse(List<T> tasks, long totalRecords) {
        this.tasks = tasks;
        this.totalRecords = totalRecords;
    }

    public List<T> getTasks() {
        return tasks;
    }

    public long getTotalRecords() {
        return totalRecords;
    }
}
