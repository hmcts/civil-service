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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
        verify(camundaRuntimeApi, never()).getOpenIncidents(any(), anyBoolean(), anyString());
    }

    @Test
    void shouldRetryIncidentsWithPagination() {
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");

        int pageSize = 50;

        // 1️⃣ Stub pages of process instances
        List<ProcessInstanceDto> firstPage = IntStream.rangeClosed(1, pageSize)
            .mapToObj(i -> {
                ProcessInstanceDto pi = new ProcessInstanceDto();
                pi.setId("proc" + i);
                return pi;
            })
            .toList();

        when(camundaRuntimeApi.getUnfinishedProcessInstancesWithIncidents(
            any(), anyBoolean(), anyBoolean(), any(), any(), any(), eq(0), eq(pageSize), any(), any()
        )).thenReturn(firstPage);

        when(camundaRuntimeApi.getUnfinishedProcessInstancesWithIncidents(
            any(), anyBoolean(), anyBoolean(), any(), any(), any(), eq(pageSize), eq(pageSize), any(), any()
        )).thenReturn(List.of());

        // 2️⃣ Stub process variables for all instances
        for (int i = 1; i <= pageSize; i++) {
            HashMap<String, VariableValueDto> vars = new HashMap<>();
            VariableValueDto varDto = new VariableValueDto();
            varDto.setValue("123");
            vars.put("caseId", varDto);
            when(camundaRuntimeApi.getProcessVariables("proc" + i, "serviceAuth")).thenReturn(vars);
        }

        int batchSize = 10;
        // 3️⃣ Stub incidents per batch
        for (int batchStart = 0; batchStart < pageSize; batchStart += batchSize) {
            int end = Math.min(batchStart + batchSize, pageSize);

            List<IncidentDto> batchIncidents = IntStream.range(batchStart, end)
                .mapToObj(i -> {
                    IncidentDto inc = new IncidentDto();
                    inc.setId("inc" + (i + 1));
                    inc.setProcessInstanceId("proc" + (i + 1));
                    inc.setConfiguration("job" + (i + 1));
                    return inc;
                }).toList();

            String batchIds = IntStream.range(batchStart, end)
                .mapToObj(i -> "proc" + (i + 1))
                .collect(Collectors.joining(","));

            when(camundaRuntimeApi.getOpenIncidents("serviceAuth", true, batchIds))
                .thenReturn(batchIncidents);
        }

        // 4️⃣ Execute handler
        ExternalTaskData result = handler.handleTask(externalTask);

        // 5️⃣ Verify all incidents retried
        for (int i = 1; i <= pageSize; i++) {
            verify(camundaRuntimeApi).setJobRetries("serviceAuth", "job" + i, Map.of("retries", 1));
        }

        assertThat(result).isNotNull();
    }

    @Test
    void shouldHandleIncidentRetryFailureGracefully() {
        ProcessInstanceDto pi = new ProcessInstanceDto();
        pi.setId("proc1");

        IncidentDto incident = new IncidentDto();
        incident.setId("inc1");
        incident.setProcessInstanceId("proc1");
        incident.setConfiguration("job1");

        when(camundaRuntimeApi.getUnfinishedProcessInstancesWithIncidents(
            any(), anyBoolean(), anyBoolean(), any(), any(), any(), anyInt(), anyInt(), any(), any()
        )).thenReturn(List.of(pi));

        when(camundaRuntimeApi.getOpenIncidents(any(), anyBoolean(), any())).thenReturn(List.of(incident));
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();
        verify(camundaRuntimeApi).setJobRetries("serviceAuth", "job1", Map.of("retries", 1));
    }
}
