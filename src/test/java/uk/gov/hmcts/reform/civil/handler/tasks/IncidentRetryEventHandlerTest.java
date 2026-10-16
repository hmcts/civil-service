package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaIncident;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaProcessInstance;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaVariableValue;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeApi;

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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import org.mockito.Spy;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;
import uk.gov.hmcts.reform.civil.service.ExternalTaskCompletionService;

@ExtendWith(MockitoExtension.class)
class IncidentRetryEventHandlerTest {

    @Mock
    private CamundaRuntimeApi camundaRuntimeApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private ExternalTask externalTask;

    @Mock
    private CaseTaskTrackingService caseTaskTrackingService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Spy
    private EventProperties eventProperties = configuredEventProperties();

    @Spy
    private ExternalTaskCompletionService externalTaskCompletionService = new ExternalTaskCompletionService();

    @InjectMocks
    private IncidentRetryEventHandler handler;

    @SuppressWarnings("unchecked")
    private static boolean matches(Map<String, Object> req) {
        List<Map<String, Object>> instructions;
        instructions = (List<Map<String, Object>>) req.get("instructions");

        return instructions.size() == 1
            && "startAfterActivity".equals(instructions.get(0).get("type"))
            && "activity1".equals(instructions.get(0).get("activityId"));
    }

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
        List<CamundaProcessInstance> processInstances = List.of(
            newProcessInstance("proc1"), newProcessInstance("proc2")
        );

        when(camundaRuntimeApi.queryProcessInstances(
            any(), eq(0), eq(50), any(), any(), anyMap()
        )).thenReturn(processInstances);

        for (CamundaProcessInstance pi : processInstances) {
            CamundaIncident incident = newIncident(pi.getId(), "inc-" + pi.getId(), "job-" + pi.getId());
            doReturn(List.of(incident), List.of()).when(camundaRuntimeApi).getLatestOpenIncidentForProcessInstance(
                any(), anyBoolean(), eq(pi.getId()), any(), any(), anyInt()
            );

            HashMap<String, CamundaVariableValue> vars = new HashMap<>();
            CamundaVariableValue variable = new CamundaVariableValue();
            variable.setValue("case-" + pi.getId());
            vars.put("caseId", variable);
            vars.put("stateId", variable("CASE_PROGRESSION"));
            vars.put("eventId", variable("CASE_EVENT"));
            when(camundaRuntimeApi.getProcessVariables(pi.getId(), "serviceAuth")).thenReturn(vars);
        }

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();

        for (CamundaProcessInstance pi : processInstances) {
            verify(camundaRuntimeApi).modifyProcessInstance(
                eq("serviceAuth"),
                eq(pi.getId()),
                anyMap()
            );
        }
        verifyNoInteractions(caseTaskTrackingService);
    }

    @Test
    void shouldCompleteActivityWhenIncidentAlreadyProcessed() {
        // GIVEN
        CamundaProcessInstance pi = newProcessInstance("proc1");

        CamundaIncident incident = newIncident(pi.getId(), "inc1", "job1");
        incident.setIncidentMessage("422 - already processed"); // KEY FOR THIS TEST

        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentMessageLike")).thenReturn(null);

        // 1 process instance returned
        when(camundaRuntimeApi.queryProcessInstances(
            any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()))
            .thenReturn(List.of(pi));

        // Incident returned with message "already processed"
        doReturn(List.of(incident), List.of()).when(camundaRuntimeApi).getLatestOpenIncidentForProcessInstance(
            any(), anyBoolean(), eq("proc1"), anyString(), anyString(), anyInt());

        // caseId exists
        HashMap<String, CamundaVariableValue> vars = new HashMap<>();
        CamundaVariableValue var = new CamundaVariableValue();
        var.setValue("case-proc1");
        vars.put("caseId", var);
        when(camundaRuntimeApi.getProcessVariables(eq("proc1"), any()))
            .thenReturn(vars);

        handler.handleTask(externalTask);

        verify(camundaRuntimeApi).modifyProcessInstance(eq("serviceAuth"), eq("proc1"), anyMap());

        verify(camundaRuntimeApi).modifyProcessInstance(
            eq("serviceAuth"),
            eq("proc1"),
            argThat(IncidentRetryEventHandlerTest::matches)
        );
    }

    @Test
    void shouldHandleGetLatestOpenIncidentExceptionGracefully() {
        CamundaProcessInstance pi = newProcessInstance("proc1");

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
        CamundaProcessInstance pi = newProcessInstance("proc1");
        CamundaIncident incident = newIncident(pi.getId(), "inc1", "job1");

        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");

        when(camundaRuntimeApi.queryProcessInstances(
            any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()
        )).thenReturn(List.of(pi));

        when(camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
            any(), anyBoolean(), eq(pi.getId()), any(), any(), anyInt()
        )).thenReturn(List.of(incident));

        when(camundaRuntimeApi.getProcessVariables(eq("proc1"), any()))
            .thenReturn(processVariables("case-proc1", "CASE_PROGRESSION", "CASE_EVENT"));

        doThrow(new RuntimeException("Failed to modify process instance"))
            .when(camundaRuntimeApi)
            .modifyProcessInstance(any(), any(), anyMap());

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();
        verify(camundaRuntimeApi).modifyProcessInstance(any(), any(), anyMap());
        verify(caseTaskTrackingService).trackCaseTask(
            eq("case-proc1"),
            eq("incidentRetry"),
            eq("StuckCaseDetected"),
            argThat(properties -> "proc1".equals(properties.get("processInstanceId"))
                && "inc1".equals(properties.get("incidentId"))
                && "UNKNOWN".equals(properties.get("incidentMessage"))
                && "CASE_PROGRESSION".equals(properties.get("stateId"))
                && "CASE_EVENT".equals(properties.get("lastEventId"))
                && "activity1".equals(properties.get("failedActivityId"))
                && "retry_failed".equals(properties.get("retryStatus"))
                && "true".equals(properties.get("retryExhausted"))
                && "job1".equals(properties.get("jobId")))
        );
    }

    @Test
    void shouldTreatIncidentStillOpenAfterRetryAsFailed() {
        CamundaProcessInstance pi = newProcessInstance("proc1");
        CamundaIncident incident = newIncident(pi.getId(), "inc1", "job1");

        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");

        when(camundaRuntimeApi.queryProcessInstances(
            any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()
        )).thenReturn(List.of(pi));

        doReturn(List.of(incident), List.of(incident)).when(camundaRuntimeApi).getLatestOpenIncidentForProcessInstance(
            any(), anyBoolean(), eq(pi.getId()), any(), any(), anyInt()
        );

        when(camundaRuntimeApi.getProcessVariables(eq("proc1"), any()))
            .thenReturn(processVariables("case-proc1", "CASE_PROGRESSION", "CASE_EVENT"));

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();
        verify(camundaRuntimeApi).modifyProcessInstance(any(), any(), anyMap());
        verify(caseTaskTrackingService).trackCaseTask(
            eq("case-proc1"),
            eq("incidentRetry"),
            eq("StuckCaseDetected"),
            argThat(properties -> "retry_validation_failed".equals(properties.get("retryStatus"))
                && "inc1".equals(properties.get("incidentId"))
                && "proc1".equals(properties.get("processInstanceId")))
        );
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
        CamundaProcessInstance pi = newProcessInstance("proc1");
        CamundaIncident incident = newIncident(pi.getId(), "inc1", "job1");

        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentMessageLike")).thenReturn(null);

        when(camundaRuntimeApi.queryProcessInstances(any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()))
            .thenReturn(List.of(pi));
        doReturn(List.of(incident), List.of()).when(camundaRuntimeApi)
            .getLatestOpenIncidentForProcessInstance(any(), anyBoolean(), eq("proc1"), any(), any(), anyInt());
        when(camundaRuntimeApi.getProcessVariables(eq("proc1"), any())).thenReturn(new HashMap<>());

        handler.handleTask(externalTask);

        verify(camundaRuntimeApi).modifyProcessInstance(eq("serviceAuth"), eq("proc1"), anyMap());
    }

    // Helper methods
    private CamundaProcessInstance newProcessInstance(String id) {
        CamundaProcessInstance pi = new CamundaProcessInstance();
        pi.setId(id);
        return pi;
    }

    private CamundaIncident newIncident(String processInstanceId, String incidentId, String jobId) {
        CamundaIncident inc = new CamundaIncident();
        inc.setId(incidentId);
        inc.setProcessInstanceId(processInstanceId);
        inc.setConfiguration(jobId);
        inc.setActivityId("activity1");
        return inc;
    }

    private HashMap<String, CamundaVariableValue> processVariables(String caseId, String stateId, String eventId) {
        HashMap<String, CamundaVariableValue> vars = new HashMap<>();
        vars.put("caseId", variable(caseId));
        vars.put("stateId", variable(stateId));
        vars.put("eventId", variable(eventId));
        return vars;
    }

    private CamundaVariableValue variable(String value) {
        CamundaVariableValue variable = new CamundaVariableValue();
        variable.setValue(value);
        return variable;
    }

    private static EventProperties configuredEventProperties() {
        EventProperties properties = new EventProperties();
        properties.setRetryCount(3);
        return properties;
    }

}
