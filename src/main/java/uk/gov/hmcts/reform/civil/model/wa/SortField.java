package uk.gov.hmcts.reform.civil.model.wa;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SortField {

    DUE_DATE("due_date"),
    TASK_TITLE("task_title"),
    LOCATION_NAME("location_name"),
    CASE_CATEGORY("case_category"),
    CASE_ID("case_id"),
    CASE_NAME("case_name"),
    NEXT_HEARING_DATE("next_hearing_date"),
    MAJOR_PRIORITY("majorPriority"),
    PRIORITY_DATE("priorityDate"),
    MINOR_PRIORITY("minorPriority"),
    TASK_ID("taskId");

    @JsonValue
    private final String id;

    SortField(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return this.id;
    }
}
