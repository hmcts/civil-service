package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.service.TelemetryService;
import uk.gov.hmcts.reform.civil.service.search.ElasticSearchService;

import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
        CaseDetails case1 = CaseDetails.builder().id(1L).build();
        CaseDetails case2 = CaseDetails.builder().id(2L).build();
        when(searchService.getCases()).thenReturn(Set.of(case1, case2));

        // Throw exception for first case, succeed for second
        doThrow(new RuntimeException("Lock conflict")).when(scheduledTask).accept(case1);
        when(errorCategorizer.categorizeError(any(RuntimeException.class))).thenReturn("lock conflict");

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
}
