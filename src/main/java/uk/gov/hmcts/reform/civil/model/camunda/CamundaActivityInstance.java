package uk.gov.hmcts.reform.civil.model.camunda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Camunda REST representation of a node in the activity instance tree.
 *
 * <p>Replaces {@code org.camunda.community.rest.client.model.ActivityInstanceDto} so the build does
 * not depend on the Holunda REST client, which is not compatible with Spring Boot 4.</p>
 *
 * <p>The tree is recursive, and only the first level is read today: the testing support client takes
 * the first child activity instance and reads its incident ids.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CamundaActivityInstance {

    private String id;
    private String activityId;
    private String activityName;
    private String activityType;
    private String processInstanceId;
    private String processDefinitionId;
    private List<String> incidentIds;
    private List<CamundaActivityInstance> childActivityInstances;
}
