package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.TelemetryService;

import java.util.Map;

@Component
@AllArgsConstructor
public class ScheduledEventTracker {

    private final ErrorCategorizer errorCategorizer;
    private final TelemetryService telemetryService;

    public void jobStartedEvent(ScheduledTaskEventConfiguration eventConfig, int casesSize) {
        telemetryService.trackEvent(
            eventConfig.getJobStartedEvent(),
            Map.of(
                "schedulerName", eventConfig.getSchedulerName(),
                "totalCases", String.valueOf(casesSize)
            )
        );
    }

    public void caseProcessedEvent(ScheduledTaskEventConfiguration eventConfig, Long caseId) {
        telemetryService.trackEvent(
            eventConfig.getCaseProcessedEvent(),
            Map.of(
                "schedulerName", eventConfig.getSchedulerName(),
                "caseId", String.valueOf(caseId),
                "status", "SUCCESS"
            )
        );
    }

    public void caseFailedEvent(ScheduledTaskEventConfiguration eventConfig, Long caseId, Exception e) {
        telemetryService.trackEvent(
            eventConfig.getCaseFailedEvent(), Map.of(
                "schedulerName", eventConfig.getSchedulerName(),
                "caseId", String.valueOf(caseId),
                "status", "FAILURE",
                "error", e.getMessage(),
                "errorCategory", errorCategorizer.categorizeError(e)
            )
        );
    }

    public void jobCompletedEvent(ScheduledTaskEventConfiguration eventConfig,
                                  int totalCases,
                                  int succeededCases,
                                  int failedCases) {
        telemetryService.trackEvent(
            eventConfig.getJobCompletedEvent(),
            Map.of(
                "schedulerName", eventConfig.getSchedulerName(),
                "totalCases", String.valueOf(totalCases),
                "succeededCases", String.valueOf(succeededCases),
                "failedCases", String.valueOf(failedCases)
            )
        );
    }

    public void jobCompletedNoCasesEvent(ScheduledTaskEventConfiguration eventConfig) {
        telemetryService.trackEvent(
            eventConfig.getJobCompletedEvent(),
            Map.of(
                "schedulerName", eventConfig.getSchedulerName(),
                "totalCases", String.valueOf(0)
            )
        );
    }

    public void jobAbortedEvent(ScheduledTaskEventConfiguration eventConfig,
                                int totalCases,
                                int succeededCases,
                                int failedCases,
                                String reason) {
        telemetryService.trackEvent(
            eventConfig.getJobAbortedEvent(),
            Map.of(
                "schedulerName", eventConfig.getSchedulerName(),
                "totalCases", String.valueOf(totalCases),
                "succeededCases", String.valueOf(succeededCases),
                "failedCases", String.valueOf(failedCases),
                "abortReason", reason != null ? reason : "Unknown"
            )
        );
    }

    public void jobAbortedEvent(ScheduledTaskEventConfiguration eventConfig, String reason) {
        telemetryService.trackEvent(
            eventConfig.getJobAbortedEvent(),
            Map.of(
                "schedulerName", eventConfig.getSchedulerName(),
                "abortReason", reason != null ? reason : "Unknown"
            )
        );
    }
}
