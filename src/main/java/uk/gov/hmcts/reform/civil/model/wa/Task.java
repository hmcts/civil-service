package uk.gov.hmcts.reform.civil.model.wa;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static uk.gov.hmcts.reform.civil.model.wa.SystemDateProvider.DATE_TIME_FORMAT;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings({"PMD.LawOfDemeter", "PMD.TooManyFields",
    "PMD.ExcessiveParameterList", "PMD.ShortClassName", "PMD.LinguisticNaming"})
@Schema(allowableValues = "Task")
@AllArgsConstructor
public class Task {

    public static final String SAMPLE_ISO_DATE_TIME = "2020-09-05T14:47:01.250542+01:00";
    @Schema(
        requiredMode = REQUIRED,
        description = "Unique identifier for the task"
    )
    private final String id;
    @Schema(
        requiredMode = REQUIRED,
        description = "Name of the task assigned in the process model"
    )
    private final String name;
    @Schema(
        requiredMode = REQUIRED,
        description = "The single user who has been assigned this task i.e. IDAM ID"
    )
    private final String assignee;
    @Schema(
        requiredMode = REQUIRED,
        description = "Unique identifier for the conceptual business task"
    )
    private final String type;
    @Schema(
        name = "task_state",
        requiredMode = REQUIRED,
        description = "unconfigured, unassigned, configured, assigned, referred, completed, cancelled"
    )
    private final String taskState;
    @Schema(
        name = "task_system",
        requiredMode = REQUIRED,
        description = " Code indicating the system which is responsible for this task. For MVP will be always SELF"
    )
    private final String taskSystem;
    @Schema(
        name = "security_classification",
        requiredMode = REQUIRED,
        description = "The security classification of the main business entity this task relates to."
            + " Can be PUBLIC, PRIVATE, RESTRICTED"
    )
    private final String securityClassification;
    @Schema(
        name = "task_title",
        requiredMode = REQUIRED,
        description = "Task title to display in task list UI"
    )
    private final String taskTitle;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = DATE_TIME_FORMAT)
    @Schema(
        name = "created_date",
        example = SAMPLE_ISO_DATE_TIME,
        description = "Optional due date for the task that will be created"
    )
    private final ZonedDateTime createdDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = DATE_TIME_FORMAT)
    @Schema(
        name = "due_date",
        example = SAMPLE_ISO_DATE_TIME,
        description = "Optional due date for the task that will be created"
    )
    private final ZonedDateTime dueDate;
    @Schema(
        name = "location_name",
        requiredMode = REQUIRED,
        description = "`location to display in task list UI"
    )
    private final String locationName;
    @Schema(requiredMode = REQUIRED,
        description = "The ePims ID for the physical location"
    )
    private final String location;
    @Schema(
        name = "execution_type",
        requiredMode = REQUIRED,
        description = "Indicator to the user interface of how this task is to be executed. "
            + "For MVP, this will always be \"Case Management Task\""
    )
    private final String executionType;
    @Schema(requiredMode = REQUIRED,
        description = "For MVP, will always be \"IA\""
    )
    private final String jurisdiction;
    @Schema(requiredMode = REQUIRED,
        description = " The region ID. For IAC is always \"1\" (national)"
    )
    private final String region;
    @Schema(
        name = "case_type_id",
        requiredMode = REQUIRED,
        description = " The CCD case type ID"
    )
    private final String caseTypeId;
    @Schema(
        name = "case_id",
        requiredMode = REQUIRED,
        description = " Case ID to display in task list UI"
    )
    private final String caseId;
    @Schema(
        name = "case_category",
        requiredMode = REQUIRED,
        description = " Case category  to display in task list UI"
    )
    private final String caseCategory;
    @Schema(
        name = "case_name",
        requiredMode = REQUIRED,
        description = " Case name to display in task list UI"
    )
    private final String caseName;
    @Schema(
        name = "auto_assigned",
        requiredMode = REQUIRED,
        description = "If TRUE then task was auto-assigned, otherwise FALSE"
    )
    private final boolean autoAssigned;

    @Schema(
        description = "boolean to show if a warning is applied to task by a service task in a subprocess")
    private final Boolean warnings;

    @Schema(
        name = "warning_list",
        description = "A list of values containing a warning code and warning text")
    private final WarningValues warningList;

    @Schema(
        name = "case_management_category",
        description = "A value describing the category of the case, for IA, "
            + "it has the same value as the AppealType field")
    private final String caseManagementCategory;

    @Schema(
        name = "work_type_id",
        requiredMode = REQUIRED,
        description = "A value containing the work type id for this task, for IA")
    private final String workTypeId;

    @Schema(
        name = "work_type_label",
        requiredMode = REQUIRED,
        description = "A value containing the work type label for this task, for IA")
    private final String workTypeLabel;

    @Schema(requiredMode = REQUIRED,
        description = "A value describing the task permissions")
    private final TaskPermissions permissions;
    @Schema(requiredMode = REQUIRED,
        description = "A value describing to users what they should do next")
    private final String description;

    @Schema(
        name = "role_category",
        requiredMode = REQUIRED,
        description = "A value describing the role category")
    private final String roleCategory;

    @Schema(
        name = "additional_properties",
        requiredMode = REQUIRED,
        description = "A value describing the additional properties")
    private final Map<String, String> additionalProperties;
    @Schema(name = "next_hearing_id", description = "Next hearing identifier")
    private final String nextHearingId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = DATE_TIME_FORMAT)
    @Schema(
        name = "next_hearing_date",
        example = SAMPLE_ISO_DATE_TIME,
        description = "Next hearing date time"
    )
    private final ZonedDateTime nextHearingDate;

    @Schema(
        name = "minor_priority",
        requiredMode = REQUIRED,
        description = "A value to be able to sort by priority")
    private final Integer minorPriority;

    @Schema(
        name = "major_priority",
        requiredMode = REQUIRED,
        description = "A value to be able to sort by priority")
    private final Integer majorPriority;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = DATE_TIME_FORMAT)
    @Schema(
        name = "priority_date",
        requiredMode = REQUIRED,
        description = "A value to be able to sort by priority")
    private final ZonedDateTime priorityDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = DATE_TIME_FORMAT)
    @Schema(
        name = "reconfigure_request_time",
        example = SAMPLE_ISO_DATE_TIME,
        description = "Optional reconfigure request time"
    )
    private ZonedDateTime reconfigureRequestTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = DATE_TIME_FORMAT)
    @Schema(
        name = "last_reconfiguration_time",
        example = SAMPLE_ISO_DATE_TIME,
        description = "Optional last reconfiguration request time"
    )
    private ZonedDateTime lastReconfigurationTime;

    @Schema(name = "termination_process", description = "Termination Process")
    private String terminationProcess;

    @JsonCreator
    public Task(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("assignee") String assignee,
        @JsonProperty("type") String type,
        @JsonProperty("task_state") String taskState,
        @JsonProperty("task_system") String taskSystem,
        @JsonProperty("security_classification") String securityClassification,
        @JsonProperty("task_title") String taskTitle,
        @JsonProperty("created_date") ZonedDateTime createdDate,
        @JsonProperty("due_date") ZonedDateTime dueDate,
        @JsonProperty("location_name") String locationName,
        @JsonProperty("location") String location,
        @JsonProperty("execution_type") String executionType,
        @JsonProperty("jurisdiction") String jurisdiction,
        @JsonProperty("region") String region,
        @JsonProperty("case_type_id") String caseTypeId,
        @JsonProperty("case_id") String caseId,
        @JsonProperty("case_category") String caseCategory,
        @JsonProperty("case_name") String caseName,
        @JsonProperty("auto_assigned") boolean autoAssigned,
        @JsonProperty("warnings") Boolean warnings,
        @JsonProperty("warning_list") WarningValues warningList,
        @JsonProperty("case_management_category") String caseManagementCategory,
        @JsonProperty("work_type_id") String workTypeId,
        @JsonProperty("work_type_label") String workTypeLabel,
        @JsonProperty("permissions") TaskPermissions taskPermissions,
        @JsonProperty("role_category") String roleCategory,
        @JsonProperty("description") String description,
        @JsonProperty("additional_properties") Map<String, String> additionalProperties,
        @JsonProperty("next_hearing_id") String nextHearingId,
        @JsonProperty("next_hearing_date") ZonedDateTime nextHearingDate,
        @JsonProperty("minor_priority") Integer minorPriority,
        @JsonProperty("major_priority") Integer majorPriority,
        @JsonProperty("priority_date") ZonedDateTime priorityDate) {
        Objects.requireNonNull(id, "taskId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        this.id = id;
        this.executionType = executionType;
        this.name = name;
        this.assignee = assignee;
        this.autoAssigned = autoAssigned;
        this.caseCategory = caseCategory;
        this.caseId = caseId;
        this.type = type;
        this.taskState = taskState;
        this.taskSystem = taskSystem;
        this.locationName = locationName;
        this.securityClassification = securityClassification;
        this.taskTitle = taskTitle;
        this.createdDate = createdDate;
        this.dueDate = dueDate;
        this.caseTypeId = caseTypeId;
        this.caseName = caseName;
        this.jurisdiction = jurisdiction;
        this.region = region;
        this.location = location;
        this.warnings = warnings;
        this.warningList = warningList;
        this.caseManagementCategory = caseManagementCategory;
        this.workTypeId = workTypeId;
        this.workTypeLabel = workTypeLabel;
        this.permissions = taskPermissions;
        this.roleCategory = roleCategory;
        this.description = description;
        this.additionalProperties = additionalProperties;
        this.nextHearingId = nextHearingId;
        this.nextHearingDate = nextHearingDate;
        this.minorPriority = minorPriority;
        this.majorPriority = majorPriority;
        this.priorityDate = priorityDate;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAssignee() {
        return assignee;
    }

    public String getType() {
        return type;
    }

    public String getTaskState() {
        return taskState;
    }

    public String getTaskSystem() {
        return taskSystem;
    }

    public String getSecurityClassification() {
        return securityClassification;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public ZonedDateTime getDueDate() {
        return dueDate;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getLocation() {
        return location;
    }

    public String getExecutionType() {
        return executionType;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public String getRegion() {
        return region;
    }

    public String getCaseTypeId() {
        return caseTypeId;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getCaseCategory() {
        return caseCategory;
    }

    public String getCaseName() {
        return caseName;
    }

    public boolean isAutoAssigned() {
        return autoAssigned;
    }

    public Boolean getWarnings() {
        return warnings;
    }

    public WarningValues getWarningList() {
        return warningList;
    }

    public String getCaseManagementCategory() {
        return caseManagementCategory;
    }

    public String getWorkTypeId() {
        return workTypeId;
    }

    public String getWorkTypeLabel() {
        return workTypeLabel;
    }

    public TaskPermissions getPermissions() {
        return permissions;
    }

    public String getRoleCategory() {
        return roleCategory;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public String getNextHearingId() {
        return nextHearingId;
    }

    public ZonedDateTime getNextHearingDate() {
        return nextHearingDate;
    }

    public Integer getMinorPriority() {
        return minorPriority;
    }

    public Integer getMajorPriority() {
        return majorPriority;
    }

    public ZonedDateTime getPriorityDate() {
        return priorityDate;
    }

    public ZonedDateTime getReconfigureRequestTime() {
        return reconfigureRequestTime;
    }

    public ZonedDateTime getLastReconfigurationTime() {
        return lastReconfigurationTime;
    }

    public String getTerminationProcess() {
        return terminationProcess;
    }

    public void setTerminationProcess(String terminationProcess) {
        this.terminationProcess = terminationProcess;
    }
}
