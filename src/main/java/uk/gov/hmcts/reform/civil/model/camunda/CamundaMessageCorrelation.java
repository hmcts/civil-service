package uk.gov.hmcts.reform.civil.model.camunda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Request body for {@code POST /message}, the REST equivalent of
 * {@code runtimeService.createMessageCorrelation(...)}.
 *
 * <p>{@code processVariables} carries the variables that were previously supplied via
 * the fluent {@code setVariable} calls on the correlation builder.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CamundaMessageCorrelation {

    private String messageName;
    private String businessKey;
    private String tenantId;
    private Boolean withoutTenantId;
    private Map<String, CamundaVariableValue> processVariables;
    private Map<String, CamundaVariableValue> correlationKeys;
    private Boolean resultEnabled;
    private Boolean all;
}
