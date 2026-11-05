package uk.gov.hmcts.reform.civil.handler.tasks;

import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaIncident;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaProcessInstance;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaVariableValue;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeApi;
import uk.gov.hmcts.reform.civil.service.search.CasesStuckCheckSearchService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private CasesStuckCheckSearchService casesStuckCheckSearchService;

    @Mock
    private CaseTaskTrackingService caseTaskTrackingService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private GuardedTopicRegistry guardedTopicRegistry;

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
    void shouldCallCasesStuckCheckSearchService() {
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");
        when(externalTask.getVariable("incidentMessageLike")).thenReturn("already processed");
        when(externalTask.getVariable("stuckCasesFromPastDays")).thenReturn("8");

        when(camundaRuntimeApi.queryProcessInstances(any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()))
            .thenReturn(List.of());
        when(casesStuckCheckSearchService.getCases("8")).thenReturn(Collections.emptySet());

        handler.handleTask(externalTask);

        verify(casesStuckCheckSearchService).getCases("8");
        verifyNoInteractions(caseTaskTrackingService);
    }

    @Test
    void shouldCallCasesStuckCheckSearchServiceWithDefault() {
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");

        when(camundaRuntimeApi.queryProcessInstances(any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()))
            .thenReturn(List.of());
        when(casesStuckCheckSearchService.getCases("7")).thenReturn(Collections.emptySet());

        handler.handleTask(externalTask);

        verify(casesStuckCheckSearchService).getCases("7");
        verifyNoInteractions(caseTaskTrackingService);
    }

    @Test
    void shouldReturnEmptyData_whenNoProcessInstancesFound() {
        when(camundaRuntimeApi.queryProcessInstances(
            any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()
        )).thenReturn(List.of());

        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");
        when(casesStuckCheckSearchService.getCases("7")).thenReturn(Collections.emptySet());

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
        when(casesStuckCheckSearchService.getCases("7")).thenReturn(Collections.emptySet());

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
        when(externalTask.getVariable("stuckCasesFromPastDays")).thenReturn("7");

        // 1 process instance returned
        when(camundaRuntimeApi.queryProcessInstances(
            any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()))
            .thenReturn(List.of(pi));
        when(casesStuckCheckSearchService.getCases("7")).thenReturn(Collections.emptySet());

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
        when(casesStuckCheckSearchService.getCases("7")).thenReturn(Collections.emptySet());

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
        when(casesStuckCheckSearchService.getCases("7")).thenReturn(caseDetailsSet(1777000000000001L));

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
        verify(caseTaskTrackingService).trackCaseTask(
            eq("MULTIPLE"),
            eq("incidentRetryDailySummary"),
            eq("StuckCasesDailyDigest"),
            argThat(properties -> "2025-01-01T00:00:00Z".equals(properties.get("incidentStartTime"))
                && "2025-12-31T23:59:59Z".equals(properties.get("incidentEndTime"))
                && "true".equals(properties.get("manualInterventionRequired"))
                && "1".equals(properties.get("stuckCaseCount"))
                && "1".equals(properties.get("failedIncidentCount"))
                && "1".equals(properties.get("totalRetries"))
                && "0".equals(properties.get("successRetries"))
                && "1".equals(properties.get("failedRetries"))
                && "1777000000000001".equals(properties.get("caseIds"))
                && "inc1".equals(properties.get("incidentIds"))
                && "proc1".equals(properties.get("processInstanceIds"))
                && "activity1".equals(properties.get("failedActivityIds")))
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
        when(casesStuckCheckSearchService.getCases("7")).thenReturn(caseDetailsSet(1777000000000001L));

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
        verify(caseTaskTrackingService).trackCaseTask(
            eq("MULTIPLE"),
            eq("incidentRetryDailySummary"),
            eq("StuckCasesDailyDigest"),
            argThat(properties -> "1".equals(properties.get("stuckCaseCount"))
                && "0".equals(properties.get("successRetries"))
                && "1".equals(properties.get("failedRetries"))
                && "1777000000000001".equals(properties.get("caseIds")))
        );
    }

    @Test
    void shouldTrackSingleDailySummaryEventForMultipleFailedCases() {
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("2025-01-01T00:00:00Z");
        when(externalTask.getVariable("incidentEndTime")).thenReturn("2025-12-31T23:59:59Z");

        List<CamundaProcessInstance> processInstances = List.of(
            newProcessInstance("proc1"), newProcessInstance("proc2")
        );

        when(camundaRuntimeApi.queryProcessInstances(
            any(), eq(0), eq(50), any(), any(), anyMap()
        )).thenReturn(processInstances);
        when(casesStuckCheckSearchService.getCases("7")).thenReturn(caseDetailsSet(1777000000000001L, 1777000000000002L));

        CamundaIncident incident1 = newIncident("proc1", "inc1", "job1");
        CamundaIncident incident2 = newIncident("proc2", "inc2", "job2");

        when(camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
            any(), anyBoolean(), eq("proc1"), any(), any(), anyInt()
        )).thenReturn(List.of(incident1));
        when(camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
            any(), anyBoolean(), eq("proc2"), any(), any(), anyInt()
        )).thenReturn(List.of(incident2));

        when(camundaRuntimeApi.getProcessVariables(eq("proc1"), any()))
            .thenReturn(processVariables("case-proc1", "CASE_PROGRESSION", "CASE_EVENT"));
        when(camundaRuntimeApi.getProcessVariables(eq("proc2"), any()))
            .thenReturn(processVariables("case-proc2", "CASE_PROGRESSION", "CASE_EVENT"));

        doThrow(new RuntimeException("Failed to modify process instance"))
            .when(camundaRuntimeApi)
            .modifyProcessInstance(any(), any(), anyMap());

        handler.handleTask(externalTask);

        verify(caseTaskTrackingService).trackCaseTask(
            eq("MULTIPLE"),
            eq("incidentRetryDailySummary"),
            eq("StuckCasesDailyDigest"),
            argThat(properties -> "2".equals(properties.get("stuckCaseCount"))
                && "2".equals(properties.get("failedIncidentCount"))
                && "2".equals(properties.get("totalRetries"))
                && "0".equals(properties.get("successRetries"))
                && "2".equals(properties.get("failedRetries"))
                && "1777000000000001,1777000000000002".equals(properties.get("caseIds"))
                && "inc1,inc2".equals(properties.get("incidentIds"))
                && "proc1,proc2".equals(properties.get("processInstanceIds"))
                && "activity1".equals(properties.get("failedActivityIds")))
        );
    }

    @Test
    void shouldUseDefaults_whenIncidentTimesAreBlank() {
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getVariable("incidentStartTime")).thenReturn("");
        when(externalTask.getVariable("incidentEndTime")).thenReturn(null);
        when(camundaRuntimeApi.queryProcessInstances(any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()))
            .thenReturn(List.of()); // no instances, just to exit early
        when(casesStuckCheckSearchService.getCases("7")).thenReturn(Collections.emptySet());

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
        when(externalTask.getVariable("stuckCasesFromPastDays")).thenReturn("7");
        when(casesStuckCheckSearchService.getCases("7")).thenReturn(Collections.emptySet());

        when(camundaRuntimeApi.queryProcessInstances(any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()))
            .thenReturn(List.of(pi));
        doReturn(List.of(incident), List.of()).when(camundaRuntimeApi)
            .getLatestOpenIncidentForProcessInstance(any(), anyBoolean(), eq("proc1"), any(), any(), anyInt());
        when(camundaRuntimeApi.getProcessVariables(eq("proc1"), any())).thenReturn(new HashMap<>());

        handler.handleTask(externalTask);

        verify(camundaRuntimeApi).modifyProcessInstance(eq("serviceAuth"), eq("proc1"), anyMap());
    }

    @Test
    void shouldTrackDailySummaryFromCasesStuckCheckResultsWhenNoRetriesFail() {
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(camundaRuntimeApi.queryProcessInstances(any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()))
            .thenReturn(List.of());
        when(casesStuckCheckSearchService.getCases("7"))
            .thenReturn(caseDetailsSet(1777022268539825L, 1777022272949457L));
        mockSearchCaseEnrichment("1777022268539825", "proc-search-1", "inc-search-1", "activity-search-1", "EVENT_1");
        mockSearchCaseEnrichment("1777022272949457", "proc-search-2", "inc-search-2", "activity-search-2", "EVENT_2");

        handler.handleTask(externalTask);

        verify(caseTaskTrackingService).trackCaseTask(
            eq("MULTIPLE"),
            eq("incidentRetryDailySummary"),
            eq("StuckCasesDailyDigest"),
            argThat(properties -> "2".equals(properties.get("stuckCaseCount"))
                && "2".equals(properties.get("failedIncidentCount"))
                && "0".equals(properties.get("totalRetries"))
                && "0".equals(properties.get("successRetries"))
                && "0".equals(properties.get("failedRetries"))
                && "1777022268539825,1777022272949457".equals(properties.get("caseIds"))
                && "inc-search-1,inc-search-2".equals(properties.get("incidentIds"))
                && "proc-search-1,proc-search-2".equals(properties.get("processInstanceIds"))
                && "activity-search-1,activity-search-2".equals(properties.get("failedActivityIds")))
        );
    }

    @Test
    void shouldTrackIndividualEventsFromCasesStuckCheckResultsWhenNoRetriesFail() {
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(camundaRuntimeApi.queryProcessInstances(any(), anyInt(), anyInt(), anyString(), anyString(), anyMap()))
            .thenReturn(List.of());
        when(casesStuckCheckSearchService.getCases("7"))
            .thenReturn(caseDetailsSet(1777022268539825L, 1777022272949457L));
        mockSearchCaseEnrichment("1777022268539825", "proc-search-1", "inc-search-1", "activity-search-1", "EVENT_1");
        mockSearchCaseEnrichment("1777022272949457", "proc-search-2", "inc-search-2", "activity-search-2", "EVENT_2");

        handler.handleTask(externalTask);

        verify(caseTaskTrackingService).trackCaseTask(
            eq("1777022268539825"),
            eq("incidentRetry"),
            eq("StuckCaseDetected"),
            argThat(properties -> "proc-search-1".equals(properties.get("processInstanceId"))
                && "inc-search-1".equals(properties.get("incidentId"))
                && "UNKNOWN".equals(properties.get("incidentMessage"))
                && "CASE_PROGRESSION".equals(properties.get("stateId"))
                && "EVENT_1".equals(properties.get("lastEventId"))
                && "activity-search-1".equals(properties.get("failedActivityId"))
                && "stuck_case_open_incident".equals(properties.get("retryStatus"))
                && "false".equals(properties.get("retryExhausted"))
                && "UNKNOWN".equals(properties.get("jobId")))
        );
        verify(caseTaskTrackingService).trackCaseTask(
            eq("1777022272949457"),
            eq("incidentRetry"),
            eq("StuckCaseDetected"),
            argThat(properties -> "proc-search-2".equals(properties.get("processInstanceId"))
                && "inc-search-2".equals(properties.get("incidentId"))
                && "EVENT_2".equals(properties.get("lastEventId"))
                && "activity-search-2".equals(properties.get("failedActivityId"))
                && "stuck_case_open_incident".equals(properties.get("retryStatus")))
        );
    }

    @Test
    void guardedOnlyMode_shouldRetryGuardedIncidentOnceAndMarkIt() {
        CamundaIncident incident = newIncident("proc1", "inc1", "task1");
        givenGuardedOnlyRun(incident, "processCaseEvent", true);
        when(camundaRuntimeApi.getProcessVariables(eq("proc1"), any()))
            .thenReturn(processVariables("case-proc1", "CASE_PROGRESSION", "CASE_EVENT"));

        handler.handleTask(externalTask);

        verify(camundaRuntimeApi).setProcessVariable(
            eq("serviceAuth"),
            eq("proc1"),
            eq(IncidentRetryEventHandler.RETRIED_MARKER_VARIABLE),
            argThat(value -> "String".equals(value.getType()) && "activity1".equals(value.getValue()))
        );
        verify(camundaRuntimeApi).modifyProcessInstance(eq("serviceAuth"), eq("proc1"), anyMap());
        // only the daily run clears the marker, so a repeat failure at the same activity is left to it
        verify(camundaRuntimeApi, never()).deleteProcessVariable(any(), any(), any());
        verify(camundaRuntimeApi, never()).queryProcessInstances(
            any(), anyInt(), anyInt(), anyString(), anyString(), anyMap());
    }

    @Test
    void guardedOnlyMode_shouldCapLookbackWindowAt24Hours() {
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getAllVariables()).thenReturn(Map.of(
            "retryMode", "GUARDED_ONLY", "incidentLookbackMinutes", "100000"));
        when(camundaRuntimeApi.getOpenIncidentsRaisedBetween(
            any(), anyBoolean(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
            anyInt(), anyInt())).thenReturn(List.of());

        handler.handleTask(externalTask);

        assertThat(capturedLookbackMinutes()).isEqualTo(24L * 60L);
    }

    @Test
    void guardedOnlyMode_shouldUseDefaultLookbackWhenConfiguredValueIsInvalid() {
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getAllVariables()).thenReturn(Map.of(
            "retryMode", "GUARDED_ONLY", "incidentLookbackMinutes", "not-a-number"));
        when(camundaRuntimeApi.getOpenIncidentsRaisedBetween(
            any(), anyBoolean(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
            anyInt(), anyInt())).thenReturn(List.of());

        handler.handleTask(externalTask);

        assertThat(capturedLookbackMinutes()).isEqualTo(70L);
    }

    @Test
    void guardedOnlyMode_shouldRetryIncidentAtDifferentActivityAndOverwriteMarker() {
        CamundaIncident incident = newIncident("proc1", "inc2", "task2");
        givenGuardedOnlyRun(incident, "processCaseEvent", true);
        HashMap<String, CamundaVariableValue> vars =
            processVariables("case-proc1", "CASE_PROGRESSION", "CASE_EVENT");
        vars.put(IncidentRetryEventHandler.RETRIED_MARKER_VARIABLE, variable("earlierActivity"));
        when(camundaRuntimeApi.getProcessVariables(eq("proc1"), any())).thenReturn(vars);

        handler.handleTask(externalTask);

        verify(camundaRuntimeApi).setProcessVariable(
            eq("serviceAuth"),
            eq("proc1"),
            eq(IncidentRetryEventHandler.RETRIED_MARKER_VARIABLE),
            argThat(value -> "activity1".equals(value.getValue()))
        );
        verify(camundaRuntimeApi).modifyProcessInstance(eq("serviceAuth"), eq("proc1"), anyMap());
        verify(camundaRuntimeApi, never()).deleteProcessVariable(any(), any(), any());
    }

    @Test
    void guardedOnlyMode_shouldSkipIncidentWithoutActivityId() {
        CamundaIncident incident = newIncident("proc1", "inc1", "task1");
        incident.setActivityId(null);
        givenGuardedOnlyRun(incident, "processCaseEvent", true);

        handler.handleTask(externalTask);

        verify(camundaRuntimeApi, never()).setProcessVariable(any(), any(), any(), any());
        verify(camundaRuntimeApi, never()).modifyProcessInstance(any(), any(), anyMap());
    }

    @Test
    void guardedOnlyMode_failedRetryShouldKeepMarkerAndSendNoStuckCaseEvent() {
        CamundaIncident incident = newIncident("proc1", "inc1", "task1");
        givenGuardedOnlyRun(incident, "processCaseEvent", true);
        when(camundaRuntimeApi.getProcessVariables(eq("proc1"), any()))
            .thenReturn(processVariables("case-proc1", "CASE_PROGRESSION", "CASE_EVENT"));
        doThrow(new RuntimeException("Failed to modify process instance"))
            .when(camundaRuntimeApi).modifyProcessInstance(any(), any(), anyMap());

        handler.handleTask(externalTask);

        verify(camundaRuntimeApi).modifyProcessInstance(eq("serviceAuth"), eq("proc1"), anyMap());
        verify(camundaRuntimeApi, never()).deleteProcessVariable(any(), any(), any());
        verifyNoInteractions(caseTaskTrackingService);
    }

    @Test
    void guardedOnlyMode_incidentStillOpenAfterRetryShouldKeepMarkerAndSendNoStuckCaseEvent() {
        CamundaIncident incident = newIncident("proc1", "inc1", "task1");
        givenGuardedOnlyRun(incident, "processCaseEvent", true);
        when(camundaRuntimeApi.getProcessVariables(eq("proc1"), any()))
            .thenReturn(processVariables("case-proc1", "CASE_PROGRESSION", "CASE_EVENT"));
        when(camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
            any(), anyBoolean(), eq("proc1"), any(), any(), anyInt())).thenReturn(List.of(incident));

        handler.handleTask(externalTask);

        verify(camundaRuntimeApi).modifyProcessInstance(eq("serviceAuth"), eq("proc1"), anyMap());
        verify(camundaRuntimeApi, never()).deleteProcessVariable(any(), any(), any());
        verifyNoInteractions(caseTaskTrackingService);
    }

    @Test
    void guardedOnlyMode_shouldNotSendDigestOrSearchCcd() {
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getAllVariables()).thenReturn(Map.of("retryMode", "GUARDED_ONLY"));
        when(camundaRuntimeApi.getOpenIncidentsRaisedBetween(
            any(), anyBoolean(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
            anyInt(), anyInt())).thenReturn(List.of());

        ExternalTaskData result = handler.handleTask(externalTask);

        assertThat(result).isNotNull();
        verifyNoInteractions(casesStuckCheckSearchService);
        verifyNoInteractions(caseTaskTrackingService);
    }

    @Test
    void guardedOnlyMode_shouldUseConfiguredLookbackWindow() {
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getAllVariables()).thenReturn(Map.of(
            "retryMode", "GUARDED_ONLY", "incidentLookbackMinutes", "130"));
        when(camundaRuntimeApi.getOpenIncidentsRaisedBetween(
            any(), anyBoolean(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
            anyInt(), anyInt())).thenReturn(List.of());

        handler.handleTask(externalTask);

        assertThat(capturedLookbackMinutes()).isEqualTo(130L);
    }

    private long capturedLookbackMinutes() {
        org.mockito.ArgumentCaptor<String> after = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.ArgumentCaptor<String> before = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(camundaRuntimeApi).getOpenIncidentsRaisedBetween(
            eq("serviceAuth"), eq(true), eq("failedExternalTask"), after.capture(), before.capture(),
            eq("civil"), eq("incidentTimestamp"), eq("desc"), eq(0), eq(50));
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return java.time.Duration.between(
            java.time.OffsetDateTime.parse(after.getValue(), formatter),
            java.time.OffsetDateTime.parse(before.getValue(), formatter)).toMinutes();
    }

    @Test
    void guardedOnlyMode_shouldSkipIncidentWhoseTopicIsNotGuarded() {
        CamundaIncident incident = newIncident("proc1", "inc1", "task1");
        givenGuardedOnlyRun(incident, "generateLetter", false);

        handler.handleTask(externalTask);

        verify(camundaRuntimeApi, never()).setProcessVariable(any(), any(), any(), any());
        verify(camundaRuntimeApi, never()).modifyProcessInstance(any(), any(), anyMap());
    }

    @Test
    void guardedOnlyMode_shouldSkipIncidentAtActivityAlreadyRetried() {
        CamundaIncident incident = newIncident("proc1", "inc1", "task1");
        givenGuardedOnlyRun(incident, "processCaseEvent", true);
        HashMap<String, CamundaVariableValue> vars =
            processVariables("case-proc1", "CASE_PROGRESSION", "CASE_EVENT");
        vars.put(IncidentRetryEventHandler.RETRIED_MARKER_VARIABLE, variable("activity1"));
        when(camundaRuntimeApi.getProcessVariables(eq("proc1"), any())).thenReturn(vars);

        handler.handleTask(externalTask);

        verify(camundaRuntimeApi, never()).setProcessVariable(any(), any(), any(), any());
        verify(camundaRuntimeApi, never()).modifyProcessInstance(any(), any(), anyMap());
    }

    @Test
    void guardedOnlyMode_shouldNotRetryWhenMarkerCannotBeSet() {
        CamundaIncident incident = newIncident("proc1", "inc1", "task1");
        givenGuardedOnlyRun(incident, "processCaseEvent", true);
        when(camundaRuntimeApi.getProcessVariables(eq("proc1"), any()))
            .thenReturn(processVariables("case-proc1", "CASE_PROGRESSION", "CASE_EVENT"));
        doThrow(new RuntimeException("camunda down")).when(camundaRuntimeApi)
            .setProcessVariable(any(), any(), any(), any());

        handler.handleTask(externalTask);

        verify(camundaRuntimeApi, never()).modifyProcessInstance(any(), any(), anyMap());
    }

    @Test
    void guardedOnlyMode_shouldRetryOnlyLatestIncidentPerProcessInstance() {
        CamundaIncident latest = newIncident("proc1", "inc-new", "task-new");
        CamundaIncident older = newIncident("proc1", "inc-old", "task-old");
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getAllVariables()).thenReturn(Map.of("retryMode", "GUARDED_ONLY"));
        when(camundaRuntimeApi.getOpenIncidentsRaisedBetween(
            any(), anyBoolean(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
            anyInt(), anyInt())).thenReturn(List.of(latest, older));
        when(camundaRuntimeApi.getExternalTask("serviceAuth", "task-new"))
            .thenReturn(Map.of("topicName", "processCaseEvent"));
        when(guardedTopicRegistry.isGuarded("processCaseEvent")).thenReturn(true);
        when(camundaRuntimeApi.getProcessVariables(eq("proc1"), any()))
            .thenReturn(processVariables("case-proc1", "CASE_PROGRESSION", "CASE_EVENT"));

        handler.handleTask(externalTask);

        verify(camundaRuntimeApi, never()).getExternalTask(any(), eq("task-old"));
        verify(camundaRuntimeApi).modifyProcessInstance(eq("serviceAuth"), eq("proc1"), anyMap());
    }

    @Test
    void dailyMode_shouldClearRetriedMarkerWhenRetryingIncident() {
        CamundaProcessInstance pi = newProcessInstance("proc1");
        HashMap<String, CamundaVariableValue> vars =
            processVariables("case-proc1", "CASE_PROGRESSION", "CASE_EVENT");
        vars.put(IncidentRetryEventHandler.RETRIED_MARKER_VARIABLE, variable("activity1"));

        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(camundaRuntimeApi.queryProcessInstances(
            any(), anyInt(), anyInt(), anyString(), anyString(), anyMap())).thenReturn(List.of(pi));
        when(casesStuckCheckSearchService.getCases("7")).thenReturn(Collections.emptySet());
        CamundaIncident incident = newIncident(pi.getId(), "inc1", "job1");
        doReturn(List.of(incident), List.of()).when(camundaRuntimeApi).getLatestOpenIncidentForProcessInstance(
            any(), anyBoolean(), eq("proc1"), anyString(), anyString(), anyInt());
        when(camundaRuntimeApi.getProcessVariables(eq("proc1"), any())).thenReturn(vars);

        handler.handleTask(externalTask);

        verify(camundaRuntimeApi).deleteProcessVariable(
            "serviceAuth", "proc1", IncidentRetryEventHandler.RETRIED_MARKER_VARIABLE);
        verify(camundaRuntimeApi).modifyProcessInstance(eq("serviceAuth"), eq("proc1"), anyMap());
        verify(camundaRuntimeApi, never()).setProcessVariable(any(), any(), any(), any());
    }

    private void givenGuardedOnlyRun(CamundaIncident incident, String topic, boolean guarded) {
        when(authTokenGenerator.generate()).thenReturn("serviceAuth");
        when(externalTask.getAllVariables()).thenReturn(Map.of("retryMode", "GUARDED_ONLY"));
        when(camundaRuntimeApi.getOpenIncidentsRaisedBetween(
            any(), anyBoolean(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
            anyInt(), anyInt())).thenReturn(List.of(incident));
        when(camundaRuntimeApi.getExternalTask("serviceAuth", incident.getConfiguration()))
            .thenReturn(Map.of("topicName", topic));
        when(guardedTopicRegistry.isGuarded(topic)).thenReturn(guarded);
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

    private Set<CaseDetails> caseDetailsSet(Long... ids) {
        return java.util.Arrays.stream(ids)
            .map(id -> CaseDetails.builder().id(id).build())
            .collect(java.util.stream.Collectors.toSet());
    }

    private void mockSearchCaseEnrichment(String caseId,
                                          String processInstanceId,
                                          String incidentId,
                                          String activityId,
                                          String camundaEvent) {
        CaseDetails caseDetails = CaseDetails.builder().id(Long.valueOf(caseId)).build();
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(Long.valueOf(caseId))
            .ccdState(CaseState.CASE_PROGRESSION)
            .businessProcess(new BusinessProcess()
                .setProcessInstanceId(processInstanceId)
                .setActivityId(activityId)
                .setCamundaEvent(camundaEvent))
            .build();
        CamundaIncident incident = newIncident(processInstanceId, incidentId, "job-" + incidentId);
        incident.setActivityId(activityId);

        when(coreCaseDataService.getCase(Long.valueOf(caseId))).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
            any(), anyBoolean(), eq(processInstanceId), any(), any(), anyInt()
        )).thenReturn(List.of(incident));
    }

    private static EventProperties configuredEventProperties() {
        EventProperties properties = new EventProperties();
        properties.setRetryCount(3);
        return properties;
    }

}
