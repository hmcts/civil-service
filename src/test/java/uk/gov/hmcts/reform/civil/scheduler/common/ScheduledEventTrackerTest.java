package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.TelemetryService;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduledEventTrackerTest {

    @Mock
    private ErrorCategorizer errorCategorizer;
    @Mock
    private TelemetryService telemetryService;

    @InjectMocks
    private ScheduledEventTracker scheduledEventTracker;

    private ScheduledTaskEventConfiguration eventConfig;

    @BeforeEach
    void setUp() {
        eventConfig = new ScheduledTaskEventConfiguration("TestScheduler");
    }

    @Test
    void shouldTrackJobStartedEvent() {
        scheduledEventTracker.jobStartedEvent(eventConfig, 10);

        verify(telemetryService).trackEvent(
            eq("TestSchedulerJobStarted"),
            eq(Map.of(
                "schedulerName", "TestScheduler",
                "totalCases", "10"
            ))
        );
    }

    @Test
    void shouldTrackCaseProcessedEvent() {
        scheduledEventTracker.caseProcessedEvent(eventConfig, 123L);

        verify(telemetryService).trackEvent(
            eq("TestSchedulerCaseProcessed"),
            eq(Map.of(
                "schedulerName", "TestScheduler",
                "caseId", "123",
                "status", "SUCCESS"
            ))
        );
    }

    @Test
    void shouldTrackCaseFailedEvent() {
        CaseDetails caseDetails = CaseDetailsBuilder.builder().id(456L).build();
        Exception exception = new RuntimeException("Test error");
        when(errorCategorizer.categorizeError(exception)).thenReturn("TestCategory");

        scheduledEventTracker.caseFailedEvent(eventConfig, caseDetails, exception);

        verify(telemetryService).trackEvent(
            eq("TestSchedulerCaseFailed"),
            eq(Map.of(
                "schedulerName", "TestScheduler",
                "caseId", "456",
                "status", "FAILURE",
                "error", "Test error",
                "errorCategory", "TestCategory"
            ))
        );
    }

    @Test
    void shouldTrackJobCompletedEvent() {
        Set<CaseDetails> cases = Set.of(
            CaseDetailsBuilder.builder().id(1L).build(),
            CaseDetailsBuilder.builder().id(2L).build(),
            CaseDetailsBuilder.builder().id(3L).build()
        );
        List<Long> succeededCases = List.of(1L, 3L);
        List<Long> failedCases = List.of(2L);

        scheduledEventTracker.jobCompletedEvent(eventConfig, cases, succeededCases, failedCases);

        verify(telemetryService).trackEvent(
            eq("TestSchedulerJobCompleted"),
            eq(Map.of(
                "schedulerName", "TestScheduler",
                "totalCases", "3",
                "succeededCases", "2",
                "failedCases", "1"
            ))
        );
    }

    @Test
    void shouldTrackJobAbortedEvent() {
        Set<CaseDetails> cases = Set.of(
            CaseDetailsBuilder.builder().id(1L).build(),
            CaseDetailsBuilder.builder().id(2L).build()
        );
        List<Long> succeededCases = List.of();
        List<Long> failedCases = List.of(1L, 2L);

        scheduledEventTracker.jobAbortedEvent(eventConfig, cases, succeededCases, failedCases, "Aborted due to too many errors");

        verify(telemetryService).trackEvent(
            eq("TestSchedulerJobAborted"),
            eq(Map.of(
                "schedulerName", "TestScheduler",
                "totalCases", "2",
                "succeededCases", "0",
                "failedCases", "2",
                "abortReason", "Aborted due to too many errors"
            ))
        );
    }

    @Test
    void shouldTrackJobAbortedEventWithUnknownReason_whenReasonIsNull() {
        Set<CaseDetails> cases = Set.of();
        List<Long> succeededCases = List.of();
        List<Long> failedCases = List.of();

        scheduledEventTracker.jobAbortedEvent(eventConfig, cases, succeededCases, failedCases, null);

        verify(telemetryService).trackEvent(
            eq("TestSchedulerJobAborted"),
            eq(Map.of(
                "schedulerName", "TestScheduler",
                "totalCases", "0",
                "succeededCases", "0",
                "failedCases", "0",
                "abortReason", "Unknown"
            ))
        );
    }
}
