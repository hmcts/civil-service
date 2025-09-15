package uk.gov.hmcts.reform.civil.model.camunda;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentQueryRequest {
    private Boolean open;
    private List<String> processInstanceIds;

    private String incidentId;
    private String incidentType;
    private String incidentMessage;
    private String incidentMessageLike;
    private String processDefinitionId;
    private List<String> processDefinitionKeyIn;
    private String executionId;
    private String activityId;
    private String failedActivityId;
    private String causeIncidentId;
    private String rootCauseIncidentId;
    private String configuration;
    private List<String> tenantIdIn;
    private List<String> jobDefinitionIdIn;

    private OffsetDateTime incidentTimestampBefore;
    private OffsetDateTime incidentTimestampAfter;
}
