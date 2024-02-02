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

import javax.persistence.Id;
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

    @Id
    private Long id;
    private TaskListTemplate taskListTemplate;
    private String role;
    private String currentStatus;
    private String nextStatus;
    private String taskItemEn;
    private String taskItemCy;
    private String caseReference;
    private Instant createdAt;
    private Instant modifiedAt;
    private String createdBy;
    private String modifiedBy;
    private Long orderBy;
    private JsonNode data;
}
