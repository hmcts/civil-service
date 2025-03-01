package uk.gov.hmcts.reform.civil.service.camunda;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.community.rest.client.model.VariableValueDto;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.model.ExternalTaskDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * There is currently an issue with the getVariables with using the RunTimeService in org.camunda.community.
 * This class was created to handle retrieving camunda process variables directly.
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class CamundaRuntimeClient {

    private final AuthTokenGenerator authTokenGenerator;
    private final CamundaRuntimeApi camundaRestEngineApi;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        HashMap<String, VariableValueDto> variablesResponse = camundaRestEngineApi.getProcessVariables(processInstanceId, authTokenGenerator.generate());
        HashMap parsedResponse = new HashMap<String, Object>();
        variablesResponse.entrySet().stream().forEach(entry -> parsedResponse.put(entry.getKey(), entry.getValue().getValue()));
        return parsedResponse;
    }

    public List<ExternalTaskDTO> getTasksForProcessInstance(String processInstanceId) {
        return camundaRestEngineApi.getExternalTasks(processInstanceId);
    }

    public Map<String, Object> getEvaluatedDmnCourtLocations(String courtId, String caseTrackValue) {
        Map<String, Object> requestBody = Map.of("variables", Map.of(
            "caseManagementLocation", Map.of("value", courtId, "type", "string"),
            "claimTrack", Map.of("value", caseTrackValue, "type", "string")
        ));
        try {
            log.info("Evaluating court location dmn with CML: {}, and claim track: {}", courtId, caseTrackValue);
            List<Map<String, Object>> responseList = camundaRestEngineApi.evaluateDecision("wa-task-court-location-civil-civil", "civil", requestBody);

            return responseList.get(0);
        } catch (IndexOutOfBoundsException e) {
            log.info("Court epimmId missing from DMN");
        }
        return null;
    }
}
