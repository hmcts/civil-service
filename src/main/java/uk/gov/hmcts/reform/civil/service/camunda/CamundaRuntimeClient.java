package uk.gov.hmcts.reform.civil.service.camunda;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaVariableValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Retrieves Camunda process variables directly over REST rather than through a
 * RuntimeService implementation.
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class CamundaRuntimeClient {

    private final AuthTokenGenerator authTokenGenerator;
    private final CamundaRuntimeApi camundaRestEngineApi;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        HashMap<String, CamundaVariableValue> variablesResponse = camundaRestEngineApi.getProcessVariables(processInstanceId, authTokenGenerator.generate());
        HashMap parsedResponse = new HashMap<String, Object>();
        variablesResponse.entrySet().stream().forEach(entry -> parsedResponse.put(entry.getKey(), entry.getValue().getValue()));
        return parsedResponse;
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
