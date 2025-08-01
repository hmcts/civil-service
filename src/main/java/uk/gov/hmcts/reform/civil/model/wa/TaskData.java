package uk.gov.hmcts.reform.civil.model.wa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder (toBuilder = true)
@Jacksonized
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskData {

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    private String assignee;
    private String type;
    private String taskState;
    private String taskSystem;
    private String securityClassification;
    private String taskTitle;
    private String createdDate;
    private String dueDate;
    private String locationName;
    private String location;
    private String executionType;
    private String jurisdiction;
    private String region;
    private String caseTypeId;
    private String caseId;
    private String caseCategory;
    private String workTypeId;
    private String workTypeLabel;
    private String description;
    private String roleCategory;
    private int minorPriority;
    private int majorPriority;
    private String priorityDate;
    private boolean completeTask;
    @JsonProperty("additional_properties")
    private AdditionalProperties additionalProperties;
}
