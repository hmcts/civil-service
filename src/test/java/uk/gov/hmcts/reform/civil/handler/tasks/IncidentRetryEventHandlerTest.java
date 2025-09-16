package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.community.rest.client.model.IncidentDto;
import org.camunda.community.rest.client.model.ProcessInstanceDto;
import org.camunda.community.rest.client.model.VariableValueDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentRetryEventHandlerTest {

    @Mock
    private CamundaRuntimeApi camundaRuntimeApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private ExternalTask externalTask;

    @InjectMocks
    private IncidentRetryEventHandler handler;

    @Test
    void shouldReturnEmptyData_whenNoProcessInstancesFound() {
        when(camundaRuntimeApi.getUnfinishedProcessInstancesWithIncidents(
            any(), anyBoolean(), anyBoolean(), any(), any(), any(), anyInt(), anyInt(), any(), any()
        )).thenReturn(List.of());

        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();
        verify(camundaRuntimeApi, never()).getLatestOpenIncidentForProcessInstance(any(), anyBoolean(), anyString(), any(), any(), anyInt());
    }

    @Test
    void shouldRetryIncidentsForEachProcessInstance() {
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");

        // Mock process instances
        List<ProcessInstanceDto> processInstances = List.of(
            newProcessInstance("proc1"), newProcessInstance("proc2")
        );

        when(camundaRuntimeApi.getUnfinishedProcessInstancesWithIncidents(
            any(), anyBoolean(), anyBoolean(), any(), any(), any(), eq(0), eq(50), any(), any()
        )).thenReturn(processInstances);

        // Mock incidents and process variables for each instance
        for (ProcessInstanceDto pi : processInstances) {
            IncidentDto incident = newIncident(pi.getId(), "inc-" + pi.getId(), "job-" + pi.getId());
            when(camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
                any(), anyBoolean(), eq(pi.getId()), any(), any(), anyInt()
            )).thenReturn(List.of(incident));

            HashMap<String, VariableValueDto> vars = new HashMap<>();
            VariableValueDto var = new VariableValueDto();
            var.setValue("case-" + pi.getId());
            vars.put("caseId", var);
            when(camundaRuntimeApi.getProcessVariables(pi.getId(), "serviceAuth")).thenReturn(vars);
        }

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();

        for (ProcessInstanceDto pi : processInstances) {
            verify(camundaRuntimeApi).setJobRetries("serviceAuth", "job-" + pi.getId(), Map.of("retries", 1));
        }
    }

    @Test
    void shouldHandleGetLatestOpenIncidentExceptionGracefully() {
        ProcessInstanceDto pi = newProcessInstance("proc1");

        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");

        when(camundaRuntimeApi.getUnfinishedProcessInstancesWithIncidents(
            any(), anyBoolean(), anyBoolean(), any(), any(), any(), anyInt(), anyInt(), any(), any()
        )).thenReturn(List.of(pi));

        when(camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
            any(), anyBoolean(), eq(pi.getId()), any(), any(), anyInt()
        )).thenThrow(new RuntimeException("Incident API failure"));

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();
        verify(camundaRuntimeApi, never()).setJobRetries(any(), any(), any());
    }

    @Test
    void shouldHandleSetJobRetriesExceptionGracefully() {
        ProcessInstanceDto pi = newProcessInstance("proc1");

        IncidentDto incident = newIncident(pi.getId(), "inc1", "job1");

        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");

        when(camundaRuntimeApi.getUnfinishedProcessInstancesWithIncidents(
            any(), anyBoolean(), anyBoolean(), any(), any(), any(), anyInt(), anyInt(), any(), any()
        )).thenReturn(List.of(pi));

        when(camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(any(), anyBoolean(), anyString(), any(), any(), anyInt()))
            .thenReturn(List.of(incident));

        doThrow(new RuntimeException("Failed to set retries"))
            .when(camundaRuntimeApi)
            .setJobRetries(any(), any(), any());

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();
        verify(camundaRuntimeApi).setJobRetries(any(), any(), any());
    }

    // Helper methods
    private ProcessInstanceDto newProcessInstance(String id) {
        ProcessInstanceDto pi = new ProcessInstanceDto();
        pi.setId(id);
        return pi;
    }

    private IncidentDto newIncident(String processInstanceId, String incidentId, String jobId) {
        IncidentDto inc = new IncidentDto();
        inc.setId(incidentId);
        inc.setProcessInstanceId(processInstanceId);
        inc.setConfiguration(jobId);
        return inc;
    }
}
