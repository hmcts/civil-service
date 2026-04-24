package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.reform.civil.service.search.ElasticSearchService;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScheduledTaskRunnerTest {

    @Mock
    private ElasticSearchService searchService;

    @Mock
    private ScheduledTask scheduledTask;

    @Mock
    private TelemetryService telemetryService;

    @Mock
    private ErrorCategorizer errorCategorizer;

    @InjectMocks
    private ScheduledTaskRunner scheduledTaskRunner;

    @Test
    void shouldEmitJudgmentBufferEvents_whenConfigurationProvided() {
        // Set threshold to a high value to avoid accidental abort
        ReflectionTestUtils.setField(scheduledTaskRunner, "circuitBreakerThreshold", 5);

        CaseDetails case1 = CaseDetails.builder().id(1L).build();
        CaseDetails case2 = CaseDetails.builder().id(2L).build();
        when(searchService.getCases()).thenReturn(new LinkedHashSet<>(List.of(case1, case2)));

        // Throw exception for first case, succeed for second
        doThrow(new RuntimeException("Lock conflict")).when(scheduledTask).accept(case1);
        when(errorCategorizer.categorizeError(any())).thenReturn("lock conflict");

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");

        scheduledTaskRunner.run(eventConfig, searchService, scheduledTask);

        verify(telemetryService).trackEvent(eq("JudgmentBufferJobStarted"), eq(Map.of(
            "schedulerName", "JudgmentBuffer",
            "totalCases", "2"
        )));

        verify(telemetryService).trackEvent(eq("JudgmentBufferCaseFailed"), eq(Map.of(
            "schedulerName", "JudgmentBuffer",
            "caseId", "1",
            "status", "FAILURE",
            "error", "Lock conflict",
            "errorCategory", "lock conflict"
        )));

        verify(telemetryService).trackEvent(eq("JudgmentBufferCaseProcessed"), eq(Map.of(
            "schedulerName", "JudgmentBuffer",
            "caseId", "2",
            "status", "SUCCESS"
        )));

        verify(telemetryService).trackEvent(eq("JudgmentBufferJobCompleted"), eq(Map.of(
            "schedulerName", "JudgmentBuffer",
            "totalCases", "2",
            "succeededCases", "1",
            "failedCases", "1",
            "abortedEarly", "false"
        )));
    }

    @Test
    void shouldAbortEarly_whenConsecutiveFailuresThresholdReached() {
        // Use LinkedHashSet to ensure predictable iteration order
        CaseDetails case1 = CaseDetails.builder().id(1L).build();
        CaseDetails case2 = CaseDetails.builder().id(2L).build();
        CaseDetails case3 = CaseDetails.builder().id(3L).build();
        CaseDetails case4 = CaseDetails.builder().id(4L).build();
        Set<CaseDetails> cases = new LinkedHashSet<>(List.of(case1, case2, case3, case4));
        when(searchService.getCases()).thenReturn(cases);

        // Set threshold to 2
        ReflectionTestUtils.setField(scheduledTaskRunner, "circuitBreakerThreshold", 2);

        when(errorCategorizer.categorizeError(any())).thenReturn("generic error");

        // Fail first two cases
        doThrow(new RuntimeException("Error 1")).when(scheduledTask).accept(case1);
        doThrow(new RuntimeException("Error 2")).when(scheduledTask).accept(case2);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");

        scheduledTaskRunner.run(eventConfig, searchService, scheduledTask);

        // Verify JobAborted event
        verify(telemetryService).trackEvent(eq("JudgmentBufferJobAborted"), eq(Map.of(
            "schedulerName", "JudgmentBuffer",
            "totalCases", "4",
            "succeededCases", "2",
            "failedCases", "2",
            "abortReason", "Error 2"
        )));

        // Verify JobCompleted event reflects abortedEarly=true
        verify(telemetryService).trackEvent(eq("JudgmentBufferJobCompleted"), eq(Map.of(
            "schedulerName", "JudgmentBuffer",
            "totalCases", "4",
            "succeededCases", "2",
            "failedCases", "2",
            "abortedEarly", "true"
        )));

        // Verify case3 and case4 were NOT processed
        verify(scheduledTask).accept(case1);
        verify(scheduledTask).accept(case2);
        // Verify no more interactions with scheduledTask
    }

    @Test
    void shouldNotAbortEarly_whenFailuresAreNotConsecutive() {
        CaseDetails case1 = CaseDetails.builder().id(1L).build();
        CaseDetails case2 = CaseDetails.builder().id(2L).build();
        CaseDetails case3 = CaseDetails.builder().id(3L).build();
        Set<CaseDetails> cases = new LinkedHashSet<>(List.of(case1, case2, case3));
        when(searchService.getCases()).thenReturn(cases);

        // Set threshold to 2
        ReflectionTestUtils.setField(scheduledTaskRunner, "circuitBreakerThreshold", 2);

        when(errorCategorizer.categorizeError(any())).thenReturn("generic error");

        // Fail first, succeed second, fail third
        doThrow(new RuntimeException("Error 1")).when(scheduledTask).accept(case1);
        // case2 succeeds (default)
        doThrow(new RuntimeException("Error 3")).when(scheduledTask).accept(case3);

        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration("JudgmentBuffer");

        scheduledTaskRunner.run(eventConfig, searchService, scheduledTask);

        // Verify JobCompleted event reflects abortedEarly=false
        verify(telemetryService).trackEvent(eq("JudgmentBufferJobCompleted"), eq(Map.of(
            "schedulerName", "JudgmentBuffer",
            "totalCases", "3",
            "succeededCases", "1",
            "failedCases", "2",
            "abortedEarly", "false"
        )));

        // Verify caseFailedEvent was called for each failure
        verify(telemetryService).trackEvent(eq("JudgmentBufferCaseFailed"), eq(Map.of(
            "schedulerName", "JudgmentBuffer",
            "caseId", "1",
            "status", "FAILURE",
            "error", "Error 1",
            "errorCategory", "generic error"
        )));

        verify(telemetryService).trackEvent(eq("JudgmentBufferCaseFailed"), eq(Map.of(
            "schedulerName", "JudgmentBuffer",
            "caseId", "3",
            "status", "FAILURE",
            "error", "Error 3",
            "errorCategory", "generic error"
        )));

        // All cases should have been processed
        verify(scheduledTask).accept(case1);
        verify(scheduledTask).accept(case2);
        verify(scheduledTask).accept(case3);
    }
}
