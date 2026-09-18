package uk.gov.hmcts.reform.civil.service.camunda;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaVariableValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

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

    /**
     * Sets a single scalar process variable over REST, replacing
     * {@code runtimeService.setVariable(processInstanceId, name, value)}.
     *
     * <p>Only scalar values are supported. Camunda's REST API needs an explicit type,
     * and non-scalar values additionally need a serialisation format, so anything other
     * than the types below is rejected rather than written in a shape the read path
     * would not survive.</p>
     *
     * @throws IllegalArgumentException if the value is not a supported scalar type
     */
    public void setProcessVariable(String processInstanceId, String variableName, Object value) {
        camundaRestEngineApi.setProcessVariable(
            authTokenGenerator.generate(),
            processInstanceId,
            variableName,
            new CamundaVariableValue().setValue(value).setType(camundaTypeOf(variableName, value))
        );
    }

    private static String camundaTypeOf(String variableName, Object value) {
        if (value == null) {
            return "Null";
        }
        if (value instanceof String) {
            return "String";
        }
        if (value instanceof Boolean) {
            return "Boolean";
        }
        if (value instanceof Integer) {
            return "Integer";
        }
        if (value instanceof Long) {
            return "Long";
        }
        if (value instanceof Double) {
            return "Double";
        }
        throw new IllegalArgumentException(format(
            "Cannot set Camunda process variable '%s': unsupported type %s. "
                + "Only scalar variables can be set through this client.",
            variableName, value.getClass().getName()
        ));
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
