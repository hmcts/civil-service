package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.camunda.community.rest.client.api.ExternalTaskApiClient;
import org.camunda.community.rest.client.api.IncidentApiClient;
import org.camunda.community.rest.client.api.ProcessInstanceApiClient;
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
            .map(activityInstanceDto -> activityInstanceDto.getIncidentIds())
            .filter(CollectionUtils::isNotEmpty)
            .map(incidentIds -> incidentIds.get(0));
    }

    public String getIncidentMessage(String incidentId) {
        IncidentDto incidentDto = incidentApiClient.getIncident(incidentId).getBody();
        String externalTaskId = incidentDto.getConfiguration();

        return externalTaskApiClient.getExternalTaskErrorDetails(externalTaskId).getBody();
    }
}
