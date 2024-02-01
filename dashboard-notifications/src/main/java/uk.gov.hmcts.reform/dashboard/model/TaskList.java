package uk.gov.hmcts.reform.dashboard.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import uk.gov.hmcts.reform.dashboard.model.TaskListTemplate;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskList implements Serializable {
    Long id;
    @NotNull
    TaskListTemplate taskListTemplate;
    @Size(max = 256)
    String role;
    @Size(max = 256)
    String currentStatus;
    @Size(max = 256)
    String nextStatus;
    @Size(max = 256)
    String taskItemEn;
    @Size(max = 256)
    String taskItemCy;
    @Size(max = 20)
    String caseReference;
    @NotNull
    Instant createdAt;
    Instant modifiedAt;
    @Size(max = 256)
    String createdBy;
    @Size(max = 256)
    String modifiedBy;
    Long orderBy;
    JsonNode data;
}
