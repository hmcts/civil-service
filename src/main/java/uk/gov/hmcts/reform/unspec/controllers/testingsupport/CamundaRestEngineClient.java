package uk.gov.hmcts.reform.unspec.controllers.testingsupport;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;
import org.camunda.bpm.engine.rest.dto.runtime.ActivityInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.IncidentDto;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CamundaRestEngineClient {

    private final AuthTokenGenerator authTokenGenerator;
    private final CamundaRestEngineApi camundaRestEngineApi;

    public Optional<String> findIncidentByProcessInstanceId(String processInstanceId) {
        return Optional.ofNullable(
            camundaRestEngineApi.getActivityInstanceByProcessInstanceId(
                processInstanceId, authTokenGenerator.generate()))
            .map(ActivityInstanceDto::getChildActivityInstances)
            .filter(ArrayUtils::isNotEmpty)
            .map(activityInstances -> activityInstances[0])
            .map(ActivityInstanceDto::getIncidentIds)
            .filter(ArrayUtils::isNotEmpty)
            .map(incidentIds -> incidentIds[0]);
    }

    public String getIncidentMessage(String incidentId) {
        IncidentDto incidentDto = camundaRestEngineApi.getIncidentById(incidentId, authTokenGenerator.generate());
        String externalTaskId = incidentDto.getConfiguration();

        return camundaRestEngineApi.getErrorDetailsByExternalTaskId(externalTaskId, authTokenGenerator.generate());
    }
}
