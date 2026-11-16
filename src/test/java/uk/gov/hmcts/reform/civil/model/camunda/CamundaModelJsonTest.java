package uk.gov.hmcts.reform.civil.model.camunda;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the Camunda REST replacements bind to the real engine payloads.
 *
 * <p>These DTOs replace the Holunda generated models, so the field names have to match
 * Camunda's REST contract exactly. Each payload here is the shape the engine actually
 * returns, including fields we do not map, which must be ignored rather than fail.</p>
 */
class CamundaModelJsonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("process variables payload binds, and unmapped fields are ignored")
    void shouldDeserialiseProcessVariables() throws Exception {
        String json = """
            {
              "caseId":  {"value": "1234567890123456", "type": "String", "valueInfo": {}},
              "stateId": {"value": "CASE_ISSUED", "type": "String", "valueInfo": {}},
              "flowFlags": {"value": null, "type": "Null", "valueInfo": {}, "unmappedField": "ignored"}
            }
            """;

        Map<String, CamundaVariableValue> vars =
            mapper.readValue(json, new TypeReference<Map<String, CamundaVariableValue>>() {});

        assertThat(vars).hasSize(3);
        assertThat(vars.get("caseId").getValue()).isEqualTo("1234567890123456");
        assertThat(vars.get("caseId").getType()).isEqualTo("String");
        assertThat(vars.get("stateId").getValue()).isEqualTo("CASE_ISSUED");
        assertThat(vars.get("flowFlags").getValue()).isNull();
    }

    @Test
    @DisplayName("incident payload binds the fields the retry flow depends on")
    void shouldDeserialiseIncident() throws Exception {
        String json = """
            [{
              "id": "incident-1",
              "processDefinitionId": "PROCESS_CASE_EVENT:1:abc",
              "processInstanceId": "proc-1",
              "executionId": "exec-1",
              "incidentTimestamp": "2026-09-18T10:15:00.000+0100",
              "incidentType": "failedExternalTask",
              "activityId": "StartBusinessProcessTask",
              "failedActivityId": "StartBusinessProcessTask",
              "causeIncidentId": "incident-1",
              "rootCauseIncidentId": "incident-1",
              "configuration": "job-99",
              "incidentMessage": "Something failed",
              "tenantId": "civil",
              "jobDefinitionId": "not-mapped-by-us",
              "annotation": null
            }]
            """;

        List<CamundaIncident> incidents =
            mapper.readValue(json, new TypeReference<List<CamundaIncident>>() {});

        assertThat(incidents).hasSize(1);
        CamundaIncident incident = incidents.get(0);
        assertThat(incident.getId()).isEqualTo("incident-1");
        assertThat(incident.getProcessInstanceId()).isEqualTo("proc-1");
        assertThat(incident.getIncidentMessage()).isEqualTo("Something failed");
        assertThat(incident.getActivityId()).isEqualTo("StartBusinessProcessTask");
        assertThat(incident.getConfiguration()).isEqualTo("job-99");
    }

    @Test
    @DisplayName("process instance payload binds, including the links field we do not map")
    void shouldDeserialiseProcessInstance() throws Exception {
        String json = """
            [{
              "links": [],
              "id": "proc-1",
              "definitionId": "PROCESS_CASE_EVENT:1:abc",
              "businessKey": null,
              "caseInstanceId": null,
              "ended": false,
              "suspended": false,
              "tenantId": "civil"
            }]
            """;

        List<CamundaProcessInstance> instances =
            mapper.readValue(json, new TypeReference<List<CamundaProcessInstance>>() {});

        assertThat(instances).hasSize(1);
        assertThat(instances.get(0).getId()).isEqualTo("proc-1");
        assertThat(instances.get(0).getTenantId()).isEqualTo("civil");
        assertThat(instances.get(0).getEnded()).isFalse();
    }

    @Test
    @DisplayName("message correlation serialises to the shape POST /message expects")
    void shouldSerialiseMessageCorrelation() throws Exception {
        CamundaMessageCorrelation correlation = new CamundaMessageCorrelation()
            .setMessageName("queryManagementRaiseQuery")
            .setTenantId("civil")
            .setProcessVariables(Map.of(
                "caseId", new CamundaVariableValue().setValue("1234567890123456").setType("String")
            ));

        Map<String, Object> asMap = mapper.readValue(
            mapper.writeValueAsString(correlation), new TypeReference<Map<String, Object>>() {});

        assertThat(asMap).containsEntry("messageName", "queryManagementRaiseQuery");
        assertThat(asMap).containsEntry("tenantId", "civil");
        assertThat(asMap).containsKey("processVariables");
        assertThat(asMap).doesNotContainKey("businessKey");
        assertThat(asMap).doesNotContainKey("correlationKeys");

        @SuppressWarnings("unchecked")
        Map<String, Object> vars = (Map<String, Object>) asMap.get("processVariables");
        @SuppressWarnings("unchecked")
        Map<String, Object> caseId = (Map<String, Object>) vars.get("caseId");
        assertThat(caseId).containsEntry("value", "1234567890123456");
        assertThat(caseId).containsEntry("type", "String");
    }

    @Test
    @DisplayName("chained accessors return the instance so fluent construction works")
    void shouldSupportChainedAccessors() {
        CamundaVariableValue value = new CamundaVariableValue().setValue("abc").setType("String");

        assertThat(value.getValue()).isEqualTo("abc");
        assertThat(value.getType()).isEqualTo("String");
    }
}
