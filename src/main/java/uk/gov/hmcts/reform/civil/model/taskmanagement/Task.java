package uk.gov.hmcts.reform.civil.model.taskmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@EqualsAndHashCode
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("assignee")
    private String assignee;
    @JsonProperty("type")
    private String type;
    @JsonProperty("task_state")
    private String taskState;
    @JsonProperty("task_system")
    private String taskSystem;
    @JsonProperty("security_classification")
    private String securityClassification;
    @JsonProperty("task_title")
    private String taskTitle;
    @JsonProperty("created_date")
    private ZonedDateTime createdDate;
    @JsonProperty("due_date")
    private ZonedDateTime dueDate;
    @JsonProperty("location_name")
    private String locationName;
    @JsonProperty("location")
    private String location;
    @JsonProperty("execution_type")
    private String executionType;
    @JsonProperty("jurisdiction")
    private String jurisdiction;
    @JsonProperty("region")
    private String region;
    @JsonProperty("case_type_id")
    private String caseTypeId;
    @JsonProperty("case_id")
    private String caseId;
    @JsonProperty("case_category")
    private String caseCategory;
    @JsonProperty("case_name")
    private String caseName;
    @JsonProperty("auto_assigned")
    private boolean autoAssigned;
    @JsonProperty("warnings")
    private Boolean warnings;
    @JsonProperty("case_management_category")
    private String caseManagementCategory;
    @JsonProperty("work_type_id")
    private String workTypeId;
    @JsonProperty("work_type_label")
    private String workTypeLabel;
    @JsonProperty("permissions")
    private TaskPermissions permissions;
    @JsonProperty("description")
    private String description;
    @JsonProperty("role_category")
    private String roleCategory;
    @JsonProperty("additional_properties")
    private Map<String, String> additionalProperties;
    @JsonProperty("next_hearing_id")
    private String nextHearingId;
    @JsonProperty("next_hearing_date")
    private ZonedDateTime nextHearingDate;
    @JsonProperty("minor_priority")
    private Integer minorPriority;
    @JsonProperty("major_priority")
    private Integer majorPriority;
    @JsonProperty("priority_date")
    private ZonedDateTime priorityDate;
    @JsonProperty("reconfigure_request_time")
    private ZonedDateTime reconfigureRequestTime;
    @JsonProperty("last_reconfiguration_time")
    private ZonedDateTime lastReconfigurationTime;
    @JsonProperty("termination_process")
    private String terminationProcess;
}
