package uk.gov.hmcts.reform.civil.service.camunda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaMessageCorrelation;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaVariableValue;

import java.util.Collection;
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
    private final ObjectMapper objectMapper;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        HashMap<String, CamundaVariableValue> variablesResponse = camundaRestEngineApi.getProcessVariables(processInstanceId, authTokenGenerator.generate());
        HashMap parsedResponse = new HashMap<String, Object>();
        variablesResponse.entrySet().stream().forEach(entry -> parsedResponse.put(entry.getKey(), entry.getValue().getValue()));
        return parsedResponse;
    }

    /**
     * Sets a single process variable over REST, replacing
     * {@code runtimeService.setVariable(processInstanceId, name, value)}.
     */
    public void setProcessVariable(String processInstanceId, String variableName, Object value) {
        camundaRestEngineApi.setProcessVariable(
            authTokenGenerator.generate(), processInstanceId, variableName, toVariableValue(value));
    }

    /**
     * Sets several process variables in one call, replacing
     * {@code runtimeService.setVariables(processInstanceId, variables)}.
     */
    public void setProcessVariables(String processInstanceId, Map<String, Object> variables) {
        camundaRestEngineApi.setProcessVariables(
            authTokenGenerator.generate(), processInstanceId, Map.of("modifications", toVariableValues(variables)));
    }

    /**
     * Correlates a start message, replacing
     * {@code runtimeService.createMessageCorrelation(name)...correlateStartMessage()}.
     *
     * <p>A null {@code tenantId} is omitted from the request rather than sent as null,
     * matching the correlation builder calls that never set a tenant.</p>
     */
    public void correlateStartMessage(String messageName, String tenantId, Map<String, Object> variables) {
        camundaRestEngineApi.correlateMessage(
            authTokenGenerator.generate(),
            new CamundaMessageCorrelation()
                .setMessageName(messageName)
                .setTenantId(tenantId)
                .setProcessVariables(toVariableValues(variables)));
    }

    /**
     * Correlates a start message explicitly against instances with no tenant, replacing
     * {@code ...withoutTenantId().correlateStartMessage()}.
     */
    public void correlateStartMessageWithoutTenant(String messageName, Map<String, Object> variables) {
        camundaRestEngineApi.correlateMessage(
            authTokenGenerator.generate(),
            new CamundaMessageCorrelation()
                .setMessageName(messageName)
                .setWithoutTenantId(true)
                .setProcessVariables(toVariableValues(variables)));
    }

    private Map<String, CamundaVariableValue> toVariableValues(Map<String, Object> variables) {
        Map<String, CamundaVariableValue> mapped = new HashMap<>();
        variables.forEach((name, value) -> mapped.put(name, toVariableValue(value)));
        return mapped;
    }

    /**
     * Maps a Java value onto Camunda's REST variable representation.
     *
     * <p>Scalars carry their Camunda type directly. Anything else is serialised to JSON
     * and sent as an {@code Object} variable with {@code application/json} as the
     * serialisation format, which the engine deserialises on read so the value comes
     * back as a JSON array or object. Sending a complex value untyped would make the
     * engine fall back to Java serialisation.</p>
     */
    private CamundaVariableValue toVariableValue(Object value) {
        if (value == null) {
            return new CamundaVariableValue().setType("Null");
        }
        if (value instanceof String || value instanceof Boolean || value instanceof Integer
            || value instanceof Long || value instanceof Double) {
            return new CamundaVariableValue().setValue(value).setType(value.getClass().getSimpleName());
        }
        try {
            return new CamundaVariableValue()
                .setValue(objectMapper.writeValueAsString(value))
                .setType("Object")
                .setValueInfo(Map.of(
                    "objectTypeName", objectTypeNameOf(value),
                    "serializationDataFormat", "application/json"));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                format("Could not serialise Camunda process variable of type %s", value.getClass().getName()), e);
        }
    }

    private static String objectTypeNameOf(Object value) {
        if (value instanceof Collection) {
            return "java.util.ArrayList";
        }
        if (value instanceof Map) {
            return "java.util.LinkedHashMap";
        }
        return value.getClass().getName();
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
