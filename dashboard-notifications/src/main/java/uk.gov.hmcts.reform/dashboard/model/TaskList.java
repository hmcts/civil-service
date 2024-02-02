package uk.gov.hmcts.reform.dashboard.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.dashboard.model.TaskListTemplate;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskList implements Serializable {
    Long id;

    TaskListTemplate taskListTemplate;
    String role;
    String currentStatus;
    String nextStatus;
    String taskItemEn;
    String taskItemCy;
    String caseReference;
    Instant createdAt;
    Instant modifiedAt;
    String createdBy;
    String modifiedBy;
    Long orderBy;
    JsonNode data;
}
