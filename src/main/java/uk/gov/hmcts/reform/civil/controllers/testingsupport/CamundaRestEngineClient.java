package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.community.rest.client.api.ExternalTaskApiClient;
import org.camunda.community.rest.client.api.HistoricProcessInstanceApiClient;
import org.camunda.community.rest.client.api.IncidentApiClient;
import org.camunda.community.rest.client.api.ProcessDefinitionApiClient;
import org.camunda.community.rest.client.api.ProcessInstanceApiClient;
import org.camunda.community.rest.client.model.ActivityInstanceDto;
import org.camunda.community.rest.client.model.HistoricProcessInstanceDto;
import org.camunda.community.rest.client.model.IncidentDto;
import org.camunda.community.rest.client.model.ProcessInstanceWithVariablesDto;
import org.camunda.community.rest.client.model.StartProcessInstanceDto;
import org.camunda.community.rest.client.model.VariableValueDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Component
@RequiredArgsConstructor
public class CamundaRestEngineClient {

    private final ProcessInstanceApiClient processInstanceApiClient;
    private final ExternalTaskApiClient externalTaskApiClient;
    private final IncidentApiClient incidentApiClient;
    private final ProcessDefinitionApiClient processDefinitionApiClient;
    private final HistoricProcessInstanceApiClient historicInstancesClient;

    public Optional<String> findIncidentByProcessInstanceId(String processInstanceId) {
        return Optional.ofNullable(
                processInstanceApiClient.getActivityInstanceTree(
                    processInstanceId))
            .map(response -> response.getBody().getChildActivityInstances())
            .filter(CollectionUtils::isNotEmpty)
            .map(activityInstances -> activityInstances.get(0))
            .map(ActivityInstanceDto::getIncidentIds)
            .filter(CollectionUtils::isNotEmpty)
            .map(incidentIds -> incidentIds.get(0));
    }

    public String getIncidentMessage(String incidentId) {
        IncidentDto incidentDto =
            Optional.ofNullable(incidentApiClient.getIncident(incidentId).getBody())
                .orElseThrow(NotFoundException::new);
        String externalTaskId = incidentDto.getConfiguration();

        return externalTaskApiClient.getExternalTaskErrorDetails(externalTaskId).getBody();
    }

    public ResponseEntity<ProcessInstanceWithVariablesDto> startProcessByKey(String definitionKey, Map<String, Object> variables) {
        Map<String, VariableValueDto> vars = new HashMap<>();
        if (nonNull(variables)) {
            variables.entrySet().stream().forEach(entry -> vars.put(entry.getKey(), new VariableValueDto().value(entry.getValue())));
        }
        return processDefinitionApiClient.startProcessInstanceByKeyAndTenantId(definitionKey, "civil", new StartProcessInstanceDto().variables(vars));
    }

    public ResponseEntity<List<HistoricProcessInstanceDto>> getProcessInstances(String processInstanceId, String definitionKey, String variables) {
        return historicInstancesClient.getHistoricProcessInstances(
                null,
                null,
                null,
                null,
                processInstanceId,
                null,
                null,
                definitionKey,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                variables,
                null,
                null);
    }

}
