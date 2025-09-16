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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
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
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");
    }

    @Test
    void shouldReturnEmptyData_whenNoProcessInstancesFound() {
        when(externalTask.getVariable("caseIds")).thenReturn(null);
        when(camundaRuntimeApi.getUnfinishedProcessInstancesWithIncidents(
            any(), anyBoolean(), anyBoolean(), any(), any(), anyInt(), anyInt(), any(), any()
        )).thenReturn(List.of());

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();
        verify(camundaRuntimeApi, never()).getOpenIncidents(any(), anyBoolean(), any());
    }

    @Test
    void shouldRetryIncidentsWithPagination() {
        // Create two pages of process instances
        ProcessInstanceDto pi1 = new ProcessInstanceDto();
        pi1.setId("proc1");
        ProcessInstanceDto pi2 = new ProcessInstanceDto();
        pi2.setId("proc2");

        when(externalTask.getVariable("caseIds")).thenReturn("  ");

        // First page returns 2 instances
        when(camundaRuntimeApi.getUnfinishedProcessInstancesWithIncidents(
            any(), anyBoolean(), anyBoolean(), any(), any(), eq(0), eq(50), any(), any()
        )).thenReturn(List.of(pi1, pi2));

        // Second page returns empty -> end of pagination
        when(camundaRuntimeApi.getUnfinishedProcessInstancesWithIncidents(
            any(), anyBoolean(), anyBoolean(), any(), any(), eq(50), eq(50), any(), any()
        )).thenReturn(List.of());

        // Mock incidents for process instances
        IncidentDto incident1 = new IncidentDto();
        incident1.setId("inc1");
        incident1.setProcessInstanceId("proc1");
        incident1.setConfiguration("job1");

        IncidentDto incident2 = new IncidentDto();
        incident2.setId("inc2");
        incident2.setProcessInstanceId("proc2");
        incident2.setConfiguration("job2");

        when(camundaRuntimeApi.getOpenIncidents(any(), anyBoolean(), eq("proc1,proc2")))
            .thenReturn(List.of(incident1, incident2));

        // Mock variables
        HashMap<String, VariableValueDto> vars1 = new HashMap<>();
        VariableValueDto varDto1 = new VariableValueDto();
        varDto1.setValue("123");
        vars1.put("caseId", varDto1);

        when(camundaRuntimeApi.getProcessVariables("proc1", "serviceAuth")).thenReturn(vars1);
        when(camundaRuntimeApi.getProcessVariables("proc2", "serviceAuth")).thenReturn(vars1);

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();

        // Verify retries were called for both incidents
        verify(camundaRuntimeApi).setJobRetries("serviceAuth", "job1", Map.of("retries", 1));
        verify(camundaRuntimeApi).setJobRetries("serviceAuth", "job2", Map.of("retries", 1));
    }

    @Test
    void shouldHandleIncidentRetryFailureGracefully() {
        ProcessInstanceDto pi = new ProcessInstanceDto();
        pi.setId("proc1");

        IncidentDto incident = new IncidentDto();
        incident.setId("inc1");
        incident.setProcessInstanceId("proc1");
        incident.setConfiguration("job1");

        when(externalTask.getVariable("caseIds")).thenReturn("  ");
        when(camundaRuntimeApi.getUnfinishedProcessInstancesWithIncidents(
            any(), anyBoolean(), anyBoolean(), any(), any(), anyInt(), anyInt(), any(), any()
        )).thenReturn(List.of(pi));

        when(camundaRuntimeApi.getOpenIncidents(any(), anyBoolean(), any())).thenReturn(List.of(incident));
        when(camundaRuntimeApi.getProcessVariables("proc1", "serviceAuth")).thenThrow(new RuntimeException("fail"));

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();
        verify(camundaRuntimeApi).setJobRetries("serviceAuth", "job1", Map.of("retries", 1));
    }
}
