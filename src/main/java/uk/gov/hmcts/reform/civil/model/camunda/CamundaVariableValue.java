package uk.gov.hmcts.reform.civil.model.camunda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Camunda REST representation of a single process variable.
 *
 * <p>Replaces {@code org.camunda.community.rest.client.model.VariableValueDto} so the
 * build does not depend on the Holunda REST client, which is not compatible with
 * Spring Boot 4.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CamundaVariableValue {

    private Object value;
    private String type;
    private Map<String, Object> valueInfo;
}
