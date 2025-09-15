package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.community.rest.client.model.IncidentDto;
import org.camunda.community.rest.client.model.ProcessInstanceDto;
import org.camunda.community.rest.client.model.VariableValueDto;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59");
    }

    @Test
    void shouldReturnEmptyData_whenNoProcessInstancesFound() {
        when(externalTask.getVariable("caseIds")).thenReturn(null);
        when(camundaRuntimeApi.getUnfinishedProcessInstancesWithIncidents(any(), anyBoolean(), anyBoolean(), any(), any()))
            .thenReturn(List.of());

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();
        verify(camundaRuntimeApi, never()).getOpenIncidents(any(), any(), any());
    }

    @Test
    void shouldReturnEmptyData_whenNoIncidentsFound() {
        ProcessInstanceDto pi = new ProcessInstanceDto();
        pi.setId("proc1");

        when(externalTask.getVariable("caseIds")).thenReturn("123");
        when(camundaRuntimeApi.getProcessInstancesByCaseId(any(), any(), anyBoolean(), anyBoolean()))
            .thenReturn(List.of(pi));

        // Mock the new GET-based batching method
        when(camundaRuntimeApi.getOpenIncidents(any(), anyBoolean(), any()))
            .thenReturn(List.of());

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();
        verify(camundaRuntimeApi).getOpenIncidents("serviceAuth", true, "proc1");
    }

    @Test
    void shouldRetryIncidentsSuccessfully() {
        ProcessInstanceDto pi = new ProcessInstanceDto();
        pi.setId("proc1");

        IncidentDto incident = new IncidentDto();
        incident.setId("inc1");
        incident.setProcessInstanceId("proc1");
        incident.setConfiguration("job1");

        VariableValueDto variableValueDto = new VariableValueDto();
        variableValueDto.setValue("123");

        HashMap<String, VariableValueDto> variables = new HashMap<>();
        variables.put("caseId", variableValueDto);

        when(externalTask.getVariable("caseIds")).thenReturn("123");
        when(camundaRuntimeApi.getProcessInstancesByCaseId(any(), any(), anyBoolean(), anyBoolean()))
            .thenReturn(List.of(pi));

        // Mock batching to return single incident
        when(camundaRuntimeApi.getOpenIncidents(any(), anyBoolean(), any()))
            .thenReturn(List.of(incident));

        when(camundaRuntimeApi.getProcessVariables("proc1", "serviceAuth"))
            .thenReturn(variables);

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();
        verify(camundaRuntimeApi).setJobRetries("serviceAuth", "job1", Map.of("retries", 1));
    }

    @Test
    void shouldHandleRetryFailureGracefully() {
        ProcessInstanceDto pi = new ProcessInstanceDto();
        pi.setId("proc1");

        IncidentDto incident = new IncidentDto();
        incident.setId("inc1");
        incident.setProcessInstanceId("proc1");
        incident.setConfiguration("job1");

        when(externalTask.getVariable("caseIds")).thenReturn("123");
        when(camundaRuntimeApi.getProcessInstancesByCaseId(any(), any(), anyBoolean(), anyBoolean()))
            .thenReturn(List.of(pi));
        when(camundaRuntimeApi.getOpenIncidents(any(), anyBoolean(), any()))
            .thenReturn(List.of(incident));

        when(camundaRuntimeApi.getProcessVariables("proc1", "serviceAuth"))
            .thenThrow(new RuntimeException("boom!"));
        doThrow(new RuntimeException("job fail"))
            .when(camundaRuntimeApi).setJobRetries(any(), any(), any());

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();
        verify(camundaRuntimeApi).setJobRetries("serviceAuth", "job1", Map.of("retries", 1));
    }

    @Test
    void shouldFetchUnfinishedProcessInstances_whenCaseIdsEmpty() {
        when(externalTask.getVariable("caseIds")).thenReturn("  ");
        when(camundaRuntimeApi.getUnfinishedProcessInstancesWithIncidents(any(), anyBoolean(), anyBoolean(), any(), any()))
            .thenReturn(List.of());

        handler.handleTask(externalTask);

        verify(camundaRuntimeApi).getUnfinishedProcessInstancesWithIncidents(
            "serviceAuth",
            true,
            true,
            "2025-01-01T00:00:00",
            "2025-12-31T23:59:59"
        );
    }
}

