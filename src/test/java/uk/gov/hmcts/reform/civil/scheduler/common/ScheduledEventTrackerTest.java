package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.service.TelemetryService;

import java.time.Duration;
import java.util.Map;

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
            "TestSchedulerJobStarted",
            Map.of(
                "schedulerName", "TestScheduler",
                "totalCases", "10",
                "succeededCases", "0",
                "failedCases", "0"
            )
        );
    }

    @Test
    void shouldTrackCaseProcessedEvent() {
        scheduledEventTracker.caseProcessedEvent(eventConfig, 123L);

        verify(telemetryService).trackEvent(
            "TestSchedulerCaseProcessed",
            Map.of(
                "schedulerName", "TestScheduler",
                "caseId", "123",
                "status", "SUCCESS"
            )
        );
    }

    @Test
    void shouldTrackCaseProcessedEventWithMetrics() {
        Map<String, Long> metrics = Map.of("Interceptor1", 10L);
        scheduledEventTracker.caseProcessedEvent(eventConfig, "123", metrics);

        verify(telemetryService).trackEvent(
            "TestSchedulerCaseProcessed",
            Map.of(
                "schedulerName", "TestScheduler",
                "caseId", "123",
                "status", "SUCCESS",
                "metric_Interceptor1", "10"
            )
        );
    }

    @Test
    void shouldTrackCaseFailedEvent() {
        Exception exception = new RuntimeException("Test error");
        when(errorCategorizer.categorizeError(exception)).thenReturn("TestCategory");

        scheduledEventTracker.caseFailedEvent(eventConfig, 456L, exception);

        verify(telemetryService).trackEvent(
            "TestSchedulerCaseFailed",
            Map.of(
                "schedulerName", "TestScheduler",
                "caseId", "456",
                "status", "FAILURE",
                "error", "Test error",
                "errorCategory", "TestCategory"
            )
        );
    }

    @Test
    void shouldTrackCaseFailedEventWithMetrics() {
        Exception exception = new RuntimeException("Test error");
        when(errorCategorizer.categorizeError(exception)).thenReturn("TestCategory");
        Map<String, Long> metrics = Map.of("Interceptor1", 10L);

        scheduledEventTracker.caseFailedEvent(eventConfig, "456", exception, metrics);

        verify(telemetryService).trackEvent(
            "TestSchedulerCaseFailed",
            Map.of(
                "schedulerName", "TestScheduler",
                "caseId", "456",
                "status", "FAILURE",
                "error", "Test error",
                "errorCategory", "TestCategory",
                "metric_Interceptor1", "10"
            )
        );
    }

    @Test
    void shouldTrackJobCompletedEvent() {
        scheduledEventTracker.jobCompletedEvent(
            eventConfig,
            ScheduledJobReport.builder()
                .totalCases(3)
                .succeededCases(2)
                .failedCases(1)
                .abortedCases(0)
                .cumulativeDelay(Duration.ofMillis(500))
                .searchDuration(Duration.ofMillis(100))
                .processingDuration(Duration.ofMillis(400))
                .totalDuration(Duration.ofMillis(500))
                .build()
        );

        verify(telemetryService).trackEvent(
            "TestSchedulerJobCompleted",
            Map.of(
                "schedulerName", "TestScheduler",
                "totalCases", "3",
                "succeededCases", "2",
                "failedCases", "1",
                "abortedCases", "0",
                "cumulativeDelay", "500",
                "searchDuration", "100",
                "processingDuration", "400",
                "totalDuration", "500"
            )
        );
    }

    @Test
    void shouldTrackJobAbortedEvent() {
        scheduledEventTracker.jobAbortedEvent(
            eventConfig,
            ScheduledJobReport.builder()
                .totalCases(2)
                .succeededCases(0)
                .failedCases(2)
                .abortedCases(0)
                .jobAbortReason("Aborted due to too many errors")
                .cumulativeDelay(Duration.ofMillis(100))
                .searchDuration(Duration.ofMillis(50))
                .processingDuration(Duration.ofMillis(150))
                .totalDuration(Duration.ofMillis(200))
                .build()
        );

        verify(telemetryService).trackEvent(
            "TestSchedulerJobAborted",
            Map.of(
                "schedulerName", "TestScheduler",
                "totalCases", "2",
                "succeededCases", "0",
                "failedCases", "2",
                "abortedCases", "0",
                "jobAbortReason", "Aborted due to too many errors",
                "cumulativeDelay", "100",
                "searchDuration", "50",
                "processingDuration", "150",
                "totalDuration", "200"
            )
        );
    }

    @Test
    void shouldTrackJobAbortedEventWithUnknownReason_whenReasonIsNull() {
        scheduledEventTracker.jobAbortedEvent(
            eventConfig,
            ScheduledJobReport.builder()
                .totalCases(0)
                .succeededCases(0)
                .failedCases(0)
                .abortedCases(0)
                .jobAbortReason(null)
                .cumulativeDelay(Duration.ZERO)
                .searchDuration(Duration.ZERO)
                .processingDuration(Duration.ZERO)
                .totalDuration(Duration.ZERO)
                .build()
        );

        verify(telemetryService).trackEvent(
            "TestSchedulerJobAborted",
            Map.of(
                "schedulerName", "TestScheduler",
                "totalCases", "0",
                "succeededCases", "0",
                "failedCases", "0",
                "abortedCases", "0",
                "jobAbortReason", "Unknown",
                "cumulativeDelay", "0",
                "searchDuration", "0",
                "processingDuration", "0",
                "totalDuration", "0"
            )
        );
    }

    @Test
    void shouldTrackJobCompletedNoCasesEvent() {
        scheduledEventTracker.jobCompletedNoCasesEvent(eventConfig, Duration.ofMillis(100));

        verify(telemetryService).trackEvent(
            "TestSchedulerJobCompleted",
            Map.of(
                "schedulerName", "TestScheduler",
                "totalCases", "0",
                "succeededCases", "0",
                "failedCases", "0",
                "abortedCases", "0",
                "cumulativeDelay", "0",
                "searchDuration", "100",
                "processingDuration", "0",
                "totalDuration", "100"
            )
        );
    }

    @Test
    void shouldTrackCaseAbortedEvent() {
        scheduledEventTracker.caseAbortedEvent(eventConfig, "789", "Ongoing business process");

        verify(telemetryService).trackEvent(
            "TestSchedulerCaseAborted",
            Map.of(
                "schedulerName", "TestScheduler",
                "caseId", "789",
                "abortReason", "Ongoing business process",
                "status", "ABORTED"
            )
        );
    }

    @Test
    void shouldTrackCaseAbortedEventWithMetrics() {
        Map<String, Long> metrics = Map.of("Interceptor1", 10L);
        scheduledEventTracker.caseAbortedEvent(eventConfig, "789", "Ongoing business process", metrics);

        verify(telemetryService).trackEvent(
            "TestSchedulerCaseAborted",
            Map.of(
                "schedulerName", "TestScheduler",
                "caseId", "789",
                "abortReason", "Ongoing business process",
                "status", "ABORTED",
                "metric_Interceptor1", "10"
            )
        );
    }

    @Test
    void shouldTrackSimpleJobAbortedEvent() {
        scheduledEventTracker.jobAbortedEvent(eventConfig, "Error reason", Duration.ofMillis(100));

        verify(telemetryService).trackEvent(
            "TestSchedulerJobAborted",
            Map.of(
                "schedulerName", "TestScheduler",
                "totalCases", "0",
                "succeededCases", "0",
                "failedCases", "0",
                "abortedCases", "0",
                "jobAbortReason", "Error reason",
                "cumulativeDelay", "0",
                "searchDuration", "100",
                "processingDuration", "0",
                "totalDuration", "100"
            )
        );
    }

    @Test
    void shouldTrackSimpleJobAbortedEventWithUnknownReason_whenReasonIsNull() {
        scheduledEventTracker.jobAbortedEvent(eventConfig, null, Duration.ZERO);

        verify(telemetryService).trackEvent(
            "TestSchedulerJobAborted",
            Map.of(
                "schedulerName", "TestScheduler",
                "totalCases", "0",
                "succeededCases", "0",
                "failedCases", "0",
                "abortedCases", "0",
                "jobAbortReason", "Unknown",
                "cumulativeDelay", "0",
                "searchDuration", "0",
                "processingDuration", "0",
                "totalDuration", "0"
            )
        );
    }
}
