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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
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
        List<ProcessInstanceDto> processInstances = List.of(
            newProcessInstance("proc1"), newProcessInstance("proc2")
        );

        when(camundaRuntimeApi.queryProcessInstances(
            any(), eq(0), eq(50), any(), any(), anyMap()
        )).thenReturn(processInstances);
        when(casesStuckCheckSearchService.getCases("7")).thenReturn(Collections.emptySet());

        for (ProcessInstanceDto pi : processInstances) {
            IncidentDto incident = newIncident(pi.getId(), "inc-" + pi.getId(), "job-" + pi.getId());
            doReturn(List.of(incident), List.of()).when(camundaRuntimeApi).getLatestOpenIncidentForProcessInstance(
                any(), anyBoolean(), eq(pi.getId()), any(), any(), anyInt()
            );

            HashMap<String, VariableValueDto> vars = new HashMap<>();
            VariableValueDto variable = new VariableValueDto();
            variable.setValue("case-" + pi.getId());
            vars.put("caseId", variable);
            vars.put("stateId", variable("CASE_PROGRESSION"));
            vars.put("eventId", variable("CASE_EVENT"));
            when(camundaRuntimeApi.getProcessVariables(pi.getId(), "serviceAuth")).thenReturn(vars);
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
        verifyNoInteractions(caseTaskTrackingService);
    }

    @Test
    void shouldCompleteActivityWhenIncidentAlreadyProcessed() {
        // GIVEN
        ProcessInstanceDto pi = newProcessInstance("proc1");

        IncidentDto incident = newIncident(pi.getId(), "inc1", "job1");
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
        HashMap<String, VariableValueDto> vars = new HashMap<>();
        VariableValueDto var = new VariableValueDto();
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
        ProcessInstanceDto pi = newProcessInstance("proc1");

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
        ProcessInstanceDto pi = newProcessInstance("proc1");
        IncidentDto incident = newIncident(pi.getId(), "inc1", "job1");

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
                && "activity1".equals(properties.get("errorLocation"))
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
        ProcessInstanceDto pi = newProcessInstance("proc1");
        IncidentDto incident = newIncident(pi.getId(), "inc1", "job1");

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

        List<ProcessInstanceDto> processInstances = List.of(
            newProcessInstance("proc1"), newProcessInstance("proc2")
        );

        when(camundaRuntimeApi.queryProcessInstances(
            any(), eq(0), eq(50), any(), any(), anyMap()
        )).thenReturn(processInstances);
        when(casesStuckCheckSearchService.getCases("7")).thenReturn(caseDetailsSet(1777000000000001L, 1777000000000002L));

        IncidentDto incident1 = newIncident("proc1", "inc1", "job1");
        IncidentDto incident2 = newIncident("proc2", "inc2", "job2");

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
        ProcessInstanceDto pi = newProcessInstance("proc1");
        IncidentDto incident = newIncident(pi.getId(), "inc1", "job1");

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

        handler.handleTask(externalTask);

        verify(caseTaskTrackingService).trackCaseTask(
            eq("MULTIPLE"),
            eq("incidentRetryDailySummary"),
            eq("StuckCasesDailyDigest"),
            argThat(properties -> "2".equals(properties.get("stuckCaseCount"))
                && "0".equals(properties.get("failedIncidentCount"))
                && "0".equals(properties.get("totalRetries"))
                && "0".equals(properties.get("successRetries"))
                && "0".equals(properties.get("failedRetries"))
                && "1777022268539825,1777022272949457".equals(properties.get("caseIds"))
                && "".equals(properties.get("incidentIds"))
                && "".equals(properties.get("processInstanceIds"))
                && "".equals(properties.get("failedActivityIds")))
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
                && "CasesStuckCheckSearchService".equals(properties.get("errorLocation"))
                && "stuck_case_search".equals(properties.get("retryStatus"))
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
                && "stuck_case_search".equals(properties.get("retryStatus")))
        );
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

    private HashMap<String, VariableValueDto> processVariables(String caseId, String stateId, String eventId) {
        HashMap<String, VariableValueDto> vars = new HashMap<>();
        vars.put("caseId", variable(caseId));
        vars.put("stateId", variable(stateId));
        vars.put("eventId", variable(eventId));
        return vars;
    }

    private VariableValueDto variable(String value) {
        VariableValueDto variable = new VariableValueDto();
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
        IncidentDto incident = newIncident(processInstanceId, incidentId, "job-" + incidentId);
        incident.setActivityId(activityId);

        when(coreCaseDataService.getCase(Long.valueOf(caseId))).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
        when(camundaRuntimeApi.getLatestOpenIncidentForProcessInstance(
            any(), anyBoolean(), eq(processInstanceId), any(), any(), anyInt()
        )).thenReturn(List.of(incident));
    }
}
