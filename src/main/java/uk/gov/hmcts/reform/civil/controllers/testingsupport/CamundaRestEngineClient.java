package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaActivityInstance;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaHistoricProcessInstance;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaIncident;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaProcessInstance;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Component
@RequiredArgsConstructor
public class CamundaRestEngineClient {

    private static final String CIVIL_TENANT = "civil";

    private final CamundaTestingSupportApi camundaTestingSupportApi;

    public Optional<String> findIncidentByProcessInstanceId(String processInstanceId) {
        return Optional.ofNullable(camundaTestingSupportApi.getActivityInstanceTree(processInstanceId))
            .map(CamundaActivityInstance::getChildActivityInstances)
            .filter(CollectionUtils::isNotEmpty)
            .map(activityInstances -> activityInstances.get(0))
            .map(CamundaActivityInstance::getIncidentIds)
            .filter(CollectionUtils::isNotEmpty)
            .map(incidentIds -> incidentIds.get(0));
    }

    public String getIncidentMessage(String incidentId) {
        CamundaIncident incident = Optional.ofNullable(camundaTestingSupportApi.getIncident(incidentId))
            .orElseThrow(NotFoundException::new);

        return camundaTestingSupportApi.getExternalTaskErrorDetails(incident.getConfiguration());
    }

    public CamundaProcessInstance startProcessByKey(String definitionKey, Map<String, Object> variables) {
        Map<String, Object> vars = new HashMap<>();
        if (nonNull(variables)) {
            variables.forEach((name, value) -> vars.put(name, Map.of("value", value)));
        }
        return camundaTestingSupportApi.startProcessInstanceByKeyAndTenantId(
            definitionKey, CIVIL_TENANT, Map.of("variables", vars));
    }

    public List<CamundaHistoricProcessInstance> getProcessInstances(String processInstanceId,
                                                                    String definitionKey,
                                                                    String variables) {
        Map<String, Object> query = new LinkedHashMap<>();
        putIfPresent(query, "processInstanceId", processInstanceId);
        putIfPresent(query, "processDefinitionKey", definitionKey);
        List<Map<String, Object>> parsed = parseVariables(variables);
        if (nonNull(parsed)) {
            query.put("variables", parsed);
        }

        return camundaTestingSupportApi.queryHistoricProcessInstances(null, null, query);
    }

    private static void putIfPresent(Map<String, Object> target, String key, String value) {
        if (nonNull(value) && !value.isBlank()) {
            target.put(key, value);
        }
    }

    /**
     * Parses the {@code name_operator_value} form the testing support endpoint accepts, for example
     * {@code hearingId_eq_1849524557}, into the Camunda history query shape.
     */
    private List<Map<String, Object>> parseVariables(String variables) {
        if (variables == null || variables.isBlank()) {
            return null;
        }

        return Arrays.stream(variables.split(","))
            .map(expression -> expression.split("_", 3))
            .map(parts -> Map.of(
                "name", (Object) parts[0],
                "operator", parts[1],
                "value", parts[2]))
            .toList();
    }
}
