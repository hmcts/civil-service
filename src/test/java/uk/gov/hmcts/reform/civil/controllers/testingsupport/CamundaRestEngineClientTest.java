package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import org.camunda.bpm.engine.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaActivityInstance;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaIncident;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CamundaRestEngineClientTest {

    @Mock
    private CamundaTestingSupportApi api;

    private CamundaRestEngineClient client;

    @BeforeEach
    void setUp() {
        client = new CamundaRestEngineClient(api);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSerialiseVariablesIntoTheCamundaStartShapeWhenStartingProcess() {
        client.startProcessByKey("process-key", Map.of("caseId", 123L, "enabled", true));

        ArgumentCaptor<Map<String, Object>> body = ArgumentCaptor.forClass(Map.class);
        verify(api).startProcessInstanceByKeyAndTenantId(eq("process-key"), eq("civil"), body.capture());

        Map<String, Object> variables = (Map<String, Object>) body.getValue().get("variables");
        assertThat(variables)
            .as("the engine expects each variable wrapped as {\"value\": ...}, not a bare value")
            .extractingByKeys("caseId", "enabled")
            .extracting("value")
            .containsExactly(123L, true);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldQueryHistoricInstancesWithVariableFilters() {
        client.getProcessInstances("instance-id", "process-key", "caseId_eq_123,status_neq_CLOSED");

        ArgumentCaptor<Map<String, Object>> query = ArgumentCaptor.forClass(Map.class);
        verify(api).queryHistoricProcessInstances(isNull(), isNull(), query.capture());

        assertThat(query.getValue())
            .containsEntry("processInstanceId", "instance-id")
            .containsEntry("processDefinitionKey", "process-key");
        assertThat((List<Map<String, Object>>) query.getValue().get("variables"))
            .extracting(v -> v.get("name"), v -> v.get("operator"), v -> v.get("value"))
            .containsExactly(
                tuple("caseId", "eq", "123"),
                tuple("status", "neq", "CLOSED"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldOmitFiltersThatWereNotProvided() {
        client.getProcessInstances(null, "process-key", null);

        ArgumentCaptor<Map<String, Object>> query = ArgumentCaptor.forClass(Map.class);
        verify(api).queryHistoricProcessInstances(isNull(), isNull(), query.capture());

        assertThat(query.getValue())
            .as("a null filter must be left out of the body, not sent as an explicit null")
            .containsOnlyKeys("processDefinitionKey");
    }

    @Test
    void shouldFindTheFirstIncidentOnTheFirstChildActivity() {
        when(api.getActivityInstanceTree("pi-1")).thenReturn(
            new CamundaActivityInstance().setChildActivityInstances(List.of(
                new CamundaActivityInstance().setIncidentIds(List.of("incident-1", "incident-2")))));

        assertThat(client.findIncidentByProcessInstanceId("pi-1")).contains("incident-1");
    }

    @Test
    void shouldReturnEmptyWhenTheActivityTreeHasNoIncident() {
        when(api.getActivityInstanceTree("pi-1")).thenReturn(
            new CamundaActivityInstance().setChildActivityInstances(List.of(new CamundaActivityInstance())));

        assertThat(client.findIncidentByProcessInstanceId("pi-1")).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenThereIsNoActivityTreeAtAll() {
        when(api.getActivityInstanceTree("pi-1")).thenReturn(null);

        assertThat(client.findIncidentByProcessInstanceId("pi-1")).isEmpty();
    }

    /**
     * The incident's {@code configuration} holds the external task id, which is what carries the
     * stack trace. Reading the wrong field here would return an error message for a different task.
     */
    @Test
    void shouldReadErrorDetailsForTheExternalTaskNamedByTheIncidentConfiguration() {
        when(api.getIncident("incident-1"))
            .thenReturn(new CamundaIncident().setConfiguration("external-task-9"));
        when(api.getExternalTaskErrorDetails("external-task-9")).thenReturn("stack trace");

        assertThat(client.getIncidentMessage("incident-1")).isEqualTo("stack trace");
    }

    @Test
    void shouldThrowNotFoundWhenTheIncidentDoesNotExist() {
        when(api.getIncident("missing")).thenReturn(null);

        assertThatThrownBy(() -> client.getIncidentMessage("missing"))
            .isInstanceOf(NotFoundException.class);
    }
}
