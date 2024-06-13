package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.community.rest.client.api.ExternalTaskApiClient;
import org.camunda.community.rest.client.api.IncidentApiClient;
import org.camunda.community.rest.client.api.ProcessInstanceApiClient;
import org.camunda.community.rest.client.model.ActivityInstanceDto;
import org.camunda.community.rest.client.model.IncidentDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CamundaRestEngineClient {

    private final ProcessInstanceApiClient processInstanceApiClient;
    private final ExternalTaskApiClient externalTaskApiClient;
    private final IncidentApiClient incidentApiClient;

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
}
