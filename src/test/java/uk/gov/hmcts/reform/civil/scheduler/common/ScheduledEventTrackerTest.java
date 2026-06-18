package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.service.TelemetryService;

import java.util.Map;

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
                "totalCases", "10",
                "succeededCases", "0",
                "failedCases", "0"
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
        Exception exception = new RuntimeException("Test error");
        when(errorCategorizer.categorizeError(exception)).thenReturn("TestCategory");

        scheduledEventTracker.caseFailedEvent(eventConfig, 456L, exception);

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
        scheduledEventTracker.jobCompletedEvent(eventConfig, 3, 2, 1);

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
        scheduledEventTracker.jobAbortedEvent(eventConfig, 2, 0, 2, "Aborted due to too many errors");

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
        scheduledEventTracker.jobAbortedEvent(eventConfig, 0, 0, 0, null);

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

    @Test
    void shouldTrackJobCompletedNoCasesEvent() {
        scheduledEventTracker.jobCompletedNoCasesEvent(eventConfig);

        verify(telemetryService).trackEvent(
            eq("TestSchedulerJobCompleted"),
            eq(Map.of(
                "schedulerName", "TestScheduler",
                "totalCases", "0",
                "succeededCases", "0",
                "failedCases", "0"
            ))
        );
    }

    @Test
    void shouldTrackSimpleJobAbortedEvent() {
        scheduledEventTracker.jobAbortedEvent(eventConfig, "Error reason");

        verify(telemetryService).trackEvent(
            eq("TestSchedulerJobAborted"),
            eq(Map.of(
                "schedulerName", "TestScheduler",
                "totalCases", "0",
                "succeededCases", "0",
                "failedCases", "0",
                "abortReason", "Error reason"
            ))
        );
    }

    @Test
    void shouldTrackSimpleJobAbortedEventWithUnknownReason_whenReasonIsNull() {
        scheduledEventTracker.jobAbortedEvent(eventConfig, null);

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
