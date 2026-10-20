package uk.gov.hmcts.reform.civil.model.camunda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Camunda REST representation of an incident.
 *
 * <p>Replaces {@code org.camunda.community.rest.client.model.IncidentDto}. The
 * {@code configuration} field carries the job id for job based incidents, which is
 * what the retry flow uses to reset retries.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CamundaIncident {

    private String id;
    private String processInstanceId;
    private String processDefinitionId;
    private String executionId;
    private String incidentTimestamp;
    private String incidentType;
    private String incidentMessage;
    private String activityId;
    private String failedActivityId;
    private String causeIncidentId;
    private String rootCauseIncidentId;
    private String configuration;
    private String annotation;
    private String tenantId;
}
