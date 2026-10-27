package uk.gov.hmcts.reform.civil.model.camunda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Camunda REST representation of a finished or running process instance from the history service.
 *
 * <p>Replaces {@code org.camunda.community.rest.client.model.HistoricProcessInstanceDto} so the
 * build does not depend on the Holunda REST client, which is not compatible with Spring Boot 4.</p>
 *
 * <p>{@link CamundaProcessInstance} is deliberately not reused here. It models the runtime
 * representation and carries no {@code state}, and {@code state} is a contract: the functional
 * suites poll {@code /testing-support/camunda-processes} and assert {@code 0.state} is
 * {@code COMPLETED} in {@code waitForCompletedCamundaProcess}. Dropping the field would leave that
 * wait never satisfied rather than failing visibly.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CamundaHistoricProcessInstance {

    private String id;
    private String businessKey;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String processDefinitionName;
    private String startTime;
    private String endTime;
    private Long durationInMillis;
    private String state;
    private String tenantId;
}
