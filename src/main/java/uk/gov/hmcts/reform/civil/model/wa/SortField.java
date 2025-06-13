package uk.gov.hmcts.reform.civil.model.wa;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@SuppressWarnings({"squid:S1192", "PMD.AvoidDuplicateLiterals"})
@Getter
public enum SortField {

    DUE_DATE_CAMEL_CASE("dueDate", "due_date_time", "dueDateTime"),
    DUE_DATE_SNAKE_CASE("due_date", "due_date_time", "dueDateTime"),

    TASK_TITLE_CAMEL_CASE("taskTitle", "title", "title"),
    TASK_TITLE_SNAKE_CASE("task_title", "title", "title"),

    LOCATION_NAME_CAMEL_CASE("locationName", "location_name", "locationName"),
    LOCATION_NAME_SNAKE_CASE("location_name", "location_name", "locationName"),

    CASE_CATEGORY_CAMEL_CASE("caseCategory", "case_category", "caseCategory"),
    CASE_CATEGORY_SNAKE_CASE("case_category", "case_category", "caseCategory"),

    CASE_ID("caseId", "case_id", "caseId"),
    CASE_ID_SNAKE_CASE("case_id", "case_id", "caseId"),

    CASE_NAME_CAMEL_CASE("caseName", "case_name", "caseName"),
    CASE_NAME_SNAKE_CASE("case_name", "case_name", "caseName"),

    NEXT_HEARING_DATE_CAMEL_CASE("nextHearingDate", "next_hearing_date", "nextHearingDate"),
    NEXT_HEARING_DATE_SNAKE_CASE("next_hearing_date", "next_hearing_date", "nextHearingDate"),

    MAJOR_PRIORITY("majorPriority", "major_priority", "majorPriority"),
    PRIORITY_DATE("priorityDate", "priority_date", "priorityDate"),
    MINOR_PRIORITY("minorPriority", "minor_priority", "minorPriority"),
    TASK_ID("taskId", "task_id", "taskId");

    @JsonValue
    private final String id;
    private final String dbColumnName;
    private final String cftVariableName;

    SortField(String id, String dbColumnName, String cftVariableName) {
        this.id = id;
        this.dbColumnName = dbColumnName;
        this.cftVariableName = cftVariableName;
    }

    @Override
    public String toString() {
        return this.id;
    }
}
