package uk.gov.hmcts.reform.civil.model.taskmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.Map;

@Getter
@EqualsAndHashCode
@ToString
@Builder(toBuilder = true)
public class Task {

    @JsonProperty("id")
    private final String id;
    @JsonProperty("name")
    private final String name;
    @JsonProperty("assignee")
    private final String assignee;
    @JsonProperty("type")
    private final String type;
    @JsonProperty("task_state")
    private final String taskState;
    @JsonProperty("task_system")
    private final String taskSystem;
    @JsonProperty("security_classification")
    private final String securityClassification;
    @JsonProperty("task_title")
    private final String taskTitle;
    @JsonProperty("created_date")
    private final ZonedDateTime createdDate;
    @JsonProperty("due_date")
    private final ZonedDateTime dueDate;
    @JsonProperty("location_name")
    private final String locationName;
    @JsonProperty("location")
    private final String location;
    @JsonProperty("execution_type")
    private final String executionType;
    @JsonProperty("jurisdiction")
    private final String jurisdiction;
    @JsonProperty("region")
    private final String region;
    @JsonProperty("case_type_id")
    private final String caseTypeId;
    @JsonProperty("case_id")
    private final String caseId;
    @JsonProperty("case_category")
    private final String caseCategory;
    @JsonProperty("case_name")
    private final String caseName;
    @JsonProperty("auto_assigned")
    private final boolean autoAssigned;
    @JsonProperty("warnings")
    private final Boolean warnings;
    @JsonProperty("case_management_category")
    private final String caseManagementCategory;
    @JsonProperty("work_type_id")
    private final String workTypeId;
    @JsonProperty("work_type_label")
    private final String workTypeLabel;
    @JsonProperty("permissions")
    private final TaskPermissions permissions;
    @JsonProperty("description")
    private final String description;
    @JsonProperty("role_category")
    private final String roleCategory;
    @JsonProperty("additional_properties")
    private final Map<String, String> additionalProperties;
    @JsonProperty("next_hearing_id")
    private final String nextHearingId;
    @JsonProperty("next_hearing_date")
    private final ZonedDateTime nextHearingDate;
    @JsonProperty("minor_priority")
    private final Integer minorPriority;
    @JsonProperty("major_priority")
    private final Integer majorPriority;
    @JsonProperty("priority_date")
    private final ZonedDateTime priorityDate;
    @JsonProperty("reconfigure_request_time")
    private final ZonedDateTime reconfigureRequestTime;
    @JsonProperty("last_reconfiguration_time")
    private final ZonedDateTime lastReconfigurationTime;
    @JsonProperty("termination_process")
    private final String terminationProcess;
}
