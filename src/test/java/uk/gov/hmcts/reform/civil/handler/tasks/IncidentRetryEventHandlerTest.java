package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.community.rest.client.model.ActivityInstanceDto;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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
        when(camundaRuntimeApi.queryProcessInstances(
            any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()
        )).thenReturn(List.of());

        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();
        verify(camundaRuntimeApi, never()).getLatestOpenIncidentForProcessInstance(
            any(), anyBoolean(), anyString(), any(), any(), anyInt()
        );
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

        when(camundaRuntimeApi.queryProcessInstances(
            any(), eq(0), eq(50), any(), any(), anyMap()
        )).thenReturn(processInstances);

        for (ProcessInstanceDto pi : processInstances) {
            // Mock incidents
            IncidentDto incident = newIncident(pi.getId(), "inc-" + pi.getId(), "job-" + pi.getId());
            when(camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
                any(), anyBoolean(), eq(pi.getId()), any(), any(), anyInt()
            )).thenReturn(List.of(incident));

            // Mock process variables
            HashMap<String, VariableValueDto> vars = new HashMap<>();
            VariableValueDto var = new VariableValueDto();
            var.setValue("case-" + pi.getId());
            vars.put("caseId", var);
            when(camundaRuntimeApi.getProcessVariables(pi.getId(), "serviceAuth")).thenReturn(vars);

            // Mock activity instances to avoid NullPointerException
            ActivityInstanceDto tree = new ActivityInstanceDto();
            tree.setId("root-" + pi.getId());
            tree.setActivityId("activity1");
            tree.setChildActivityInstances(Collections.emptyList());
            when(camundaRuntimeApi.getActivityInstances("serviceAuth", pi.getId())).thenReturn(tree);
        }

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();

        for (ProcessInstanceDto pi : processInstances) {
            verify(camundaRuntimeApi).modifyProcessInstance(
                eq("serviceAuth"),
                eq(pi.getId()),
                anyMap()
            );
        }
    }

    @Test
    void shouldHandleGetLatestOpenIncidentExceptionGracefully() {
        ProcessInstanceDto pi = newProcessInstance("proc1");

        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");

        when(camundaRuntimeApi.queryProcessInstances(
            any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()
        )).thenReturn(List.of(pi));

        when(camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
            any(), anyBoolean(), eq(pi.getId()), any(), any(), anyInt()
        )).thenThrow(new RuntimeException("Incident API failure"));

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();
        verify(camundaRuntimeApi, never()).modifyProcessInstance(any(), any(), anyMap());
    }

    @Test
    void shouldHandleModifyProcessInstanceExceptionGracefully() {
        ProcessInstanceDto pi = newProcessInstance("proc1");
        IncidentDto incident = newIncident(pi.getId(), "inc1", "job1");
        incident.setActivityId("activity1"); // <-- important

        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");

        when(camundaRuntimeApi.queryProcessInstances(
            any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()
        )).thenReturn(List.of(pi));

        when(camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
            any(), anyBoolean(), eq(pi.getId()), any(), any(), anyInt()
        )).thenReturn(List.of(incident));

        // Mock process variables
        HashMap<String, VariableValueDto> vars = new HashMap<>();
        VariableValueDto var = new VariableValueDto();
        var.setValue("case-" + pi.getId());
        vars.put("caseId", var);
        when(camundaRuntimeApi.getProcessVariables(pi.getId(), "serviceAuth")).thenReturn(vars);

        // Mock activity instance tree
        ActivityInstanceDto tree = new ActivityInstanceDto();
        tree.setId("root-" + pi.getId());
        tree.setActivityId("activity1"); // should match incident's activityId
        tree.setChildActivityInstances(Collections.emptyList());
        when(camundaRuntimeApi.getActivityInstances("serviceAuth", pi.getId())).thenReturn(tree);

        // Simulate failure on modifyProcessInstance
        doThrow(new RuntimeException("Failed to modify process instance"))
            .when(camundaRuntimeApi)
            .modifyProcessInstance(any(), any(), anyMap());

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();
        verify(camundaRuntimeApi).modifyProcessInstance(any(), any(), anyMap());
    }

    @Test
    void shouldUseDefaults_whenIncidentTimesAreBlank() {
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("");
        when(externalTask.getVariable("incidentEndTime")).thenReturn(null);
        when(camundaRuntimeApi.queryProcessInstances(any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()))
            .thenReturn(List.of()); // no instances, just to exit early

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();
        verify(camundaRuntimeApi).queryProcessInstances(any(), eq(0), eq(50), anyString(), anyString(), anyMap());
    }

    @Test
    void shouldReturnUnknownCaseId_whenVariableMissing() {
        ProcessInstanceDto pi = newProcessInstance("proc1");
        IncidentDto incident = newIncident(pi.getId(), "inc1", "job1");

        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");

        when(camundaRuntimeApi.queryProcessInstances(
            any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()
        )).thenReturn(List.of(pi));

        when(camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
            any(), anyBoolean(), eq("proc1"), any(), any(), anyInt()
        )).thenReturn(List.of(incident));

        // Empty variables map to simulate missing caseId
        when(camundaRuntimeApi.getProcessVariables(eq("proc1"), any())).thenReturn(new HashMap<>());

        // Mock activity instance tree
        ActivityInstanceDto tree = new ActivityInstanceDto();
        tree.setId("root-proc1");
        tree.setActivityId("activity1");
        tree.setChildActivityInstances(Collections.emptyList());
        when(camundaRuntimeApi.getActivityInstances("serviceAuth", "proc1")).thenReturn(tree);

        handler.handleTask(externalTask);

        // Verify modifyProcessInstance was called despite missing caseId
        verify(camundaRuntimeApi).modifyProcessInstance(eq("serviceAuth"), eq("proc1"), anyMap());
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
        inc.setActivityId("activity1");
        return inc;
    }

    @Test
    void shouldHandleExceptionWhenFetchingCaseId() {
        ProcessInstanceDto pi = newProcessInstance("proc1");
        IncidentDto incident = newIncident(pi.getId(), "inc1", "job1");

        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");

        when(camundaRuntimeApi.queryProcessInstances(
            any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()
        )).thenReturn(List.of(pi));

        when(camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
            any(), anyBoolean(), eq("proc1"), any(), any(), anyInt()
        )).thenReturn(List.of(incident));

        // Simulate failure fetching process variables
        when(camundaRuntimeApi.getProcessVariables(eq("proc1"), any()))
            .thenThrow(new RuntimeException("DB down"));

        // Mock activity instance tree to avoid NPE
        ActivityInstanceDto tree = new ActivityInstanceDto();
        tree.setId("root-proc1");
        tree.setActivityId("activity1");
        tree.setChildActivityInstances(Collections.emptyList());
        when(camundaRuntimeApi.getActivityInstances("serviceAuth", "proc1")).thenReturn(tree);

        handler.handleTask(externalTask);

        // Verify that modifyProcessInstance is called even when fetching caseId fails
        verify(camundaRuntimeApi).modifyProcessInstance(eq("serviceAuth"), eq("proc1"), anyMap());
    }

    @Test
    void shouldApplyIncidentMessageLikeFilterWhenProvided() {
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");
        when(externalTask.getVariable("incidentMessageLike")).thenReturn("Some error");

        when(camundaRuntimeApi.queryProcessInstances(
            any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()
        )).thenReturn(List.of());

        handler.handleTask(externalTask);

        // Verify that queryProcessInstances was invoked with the filter containing incidentMessageLike
        verify(camundaRuntimeApi).queryProcessInstances(
            anyString(),
            anyInt(),
            anyInt(),
            anyString(),
            anyString(),
            argThat(filters -> filters.containsKey("incidentMessageLike")
                && "Some error".equals(filters.get("incidentMessageLike")))
        );
    }

    @Test
    void shouldRetryProcessInstance_whenActivityInstanceExists() {
        String processInstanceId = "proc1";
        String failedActivityId = "activity1";
        String serviceAuth = "serviceAuth";

        when(authTokenGenerator.generate()).thenReturn(serviceAuth);
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");

        // Mock process instance
        ProcessInstanceDto pi = newProcessInstance(processInstanceId);

        // Mock an incident
        IncidentDto incident = newIncident(processInstanceId, "inc1", "job1");
        incident.setActivityId(failedActivityId);

        // Mock process variables
        HashMap<String, VariableValueDto> variables = new HashMap<>();
        VariableValueDto var = new VariableValueDto();
        var.setValue("case-1");
        variables.put("caseId", var);

        when(camundaRuntimeApi.queryProcessInstances(any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()))
            .thenReturn(List.of(pi));
        when(camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(any(), anyBoolean(), eq(processInstanceId), any(), any(), anyInt()))
            .thenReturn(List.of(incident));
        when(camundaRuntimeApi.getProcessVariables(processInstanceId, serviceAuth))
            .thenReturn(variables);

        // Mock activity instance tree
        ActivityInstanceDto activityInstanceDto = new ActivityInstanceDto();
        activityInstanceDto.setId("runtime-activity-1");
        activityInstanceDto.setActivityId(failedActivityId);
        activityInstanceDto.setChildActivityInstances(List.of());

        when(camundaRuntimeApi.getActivityInstances(serviceAuth, processInstanceId))
            .thenReturn(activityInstanceDto);

        // Execute
        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();

        // Verify modifyProcessInstance is called with correct runtime activityInstanceId
        verify(camundaRuntimeApi).modifyProcessInstance(
            eq(serviceAuth),
            eq(processInstanceId),
            argThat(modificationRequest -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> instructions = (List<Map<String, Object>>) modificationRequest.get("instructions");
                // Should contain cancel instruction with runtime activityInstanceId
                Map<String, Object> cancelInstr = instructions.get(0);
                return "cancelActivityInstance".equals(cancelInstr.get("type"))
                    && "runtime-activity-1".equals(cancelInstr.get("activityInstanceId"));
            })
        );
    }

    @Test
    void shouldLogAndContinue_whenActivityInstanceNotFound() {
        String processInstanceId = "proc1";
        String failedActivityId = "missingActivity";
        String serviceAuth = "serviceAuth";

        when(authTokenGenerator.generate()).thenReturn(serviceAuth);
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");

        ProcessInstanceDto pi = newProcessInstance(processInstanceId);
        IncidentDto incident = newIncident(processInstanceId, "inc1", "job1");
        incident.setActivityId(failedActivityId);

        when(camundaRuntimeApi.queryProcessInstances(any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()))
            .thenReturn(List.of(pi));
        when(camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(any(), anyBoolean(), eq(processInstanceId), any(), any(), anyInt()))
            .thenReturn(List.of(incident));
        when(camundaRuntimeApi.getProcessVariables(processInstanceId, serviceAuth))
            .thenReturn(new HashMap<>());

        // Activity instance tree does NOT contain the failed activity
        ActivityInstanceDto activityInstanceDto = new ActivityInstanceDto();
        activityInstanceDto.setId("some-other-id");
        activityInstanceDto.setActivityId("otherActivity");
        activityInstanceDto.setChildActivityInstances(List.of());

        when(camundaRuntimeApi.getActivityInstances(serviceAuth, processInstanceId))
            .thenReturn(activityInstanceDto);

        handler.handleTask(externalTask);

        // Still calls modifyProcessInstance (startBeforeActivity) even if cancel cannot find runtime ID
        verify(camundaRuntimeApi).modifyProcessInstance(
            eq(serviceAuth),
            eq(processInstanceId),
            argThat(modificationRequest -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> instructions = (List<Map<String, Object>>) modificationRequest.get(
                    "instructions");
                Map<String, Object> cancelInstr = instructions.get(0);
                Map<String, Object> startInstr = instructions.get(1);
                return cancelInstr.get("activityInstanceId") == null
                    && "startBeforeActivity".equals(startInstr.get("type"))
                    && failedActivityId.equals(startInstr.get("activityId"));
            })
        );
    }
}
