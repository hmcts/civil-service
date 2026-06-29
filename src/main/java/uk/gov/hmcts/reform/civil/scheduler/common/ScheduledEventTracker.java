package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.TelemetryService;

import java.util.Map;

@Component
@AllArgsConstructor
public class ScheduledEventTracker {

    private static final String SCHEDULER_NAME = "schedulerName";
    private static final String TOTAL_CASES = "totalCases";
    private static final String SUCCEEDED_CASES = "succeededCases";
    private static final String FAILED_CASES = "failedCases";
    private static final String ABORT_REASON = "abortReason";
    private static final String UNKNOWN = "Unknown";
    private static final String ZERO = String.valueOf(0);
    private static final String FAILURE = "FAILURE";
    private static final String SUCCESS = "SUCCESS";
    private static final String CASE_ID = "caseId";
    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private static final String ERROR_CATEGORY = "errorCategory";

    private final ErrorCategorizer errorCategorizer;
    private final TelemetryService telemetryService;

    public void jobStartedEvent(ScheduledTaskEventConfiguration eventConfig, int casesSize) {
        telemetryService.trackEvent(
            eventConfig.getJobStartedEvent(),
            Map.of(
                SCHEDULER_NAME, eventConfig.getSchedulerName(),
                TOTAL_CASES, String.valueOf(casesSize),
                SUCCEEDED_CASES, ZERO,
                FAILED_CASES, ZERO
            )
        );
    }

    public void caseProcessedEvent(ScheduledTaskEventConfiguration eventConfig, Long caseId) {
        caseProcessedEvent(eventConfig, String.valueOf(caseId));
    }

    public void caseProcessedEvent(ScheduledTaskEventConfiguration eventConfig, String caseId) {
        telemetryService.trackEvent(
            eventConfig.getCaseProcessedEvent(),
            Map.of(
                SCHEDULER_NAME, eventConfig.getSchedulerName(),
                CASE_ID, caseId,
                STATUS, SUCCESS
            )
        );
    }

    public void caseFailedEvent(ScheduledTaskEventConfiguration eventConfig, Long caseId, Exception e) {
        caseFailedEvent(eventConfig, String.valueOf(caseId), e);
    }

    public void caseFailedEvent(ScheduledTaskEventConfiguration eventConfig, String caseId, Exception e) {
        telemetryService.trackEvent(
            eventConfig.getCaseFailedEvent(), Map.of(
                SCHEDULER_NAME, eventConfig.getSchedulerName(),
                CASE_ID, caseId,
                STATUS, FAILURE,
                ERROR, e.getMessage(),
                ERROR_CATEGORY, errorCategorizer.categorizeError(e)
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
                SCHEDULER_NAME, eventConfig.getSchedulerName(),
                TOTAL_CASES, String.valueOf(totalCases),
                SUCCEEDED_CASES, String.valueOf(succeededCases),
                FAILED_CASES, String.valueOf(failedCases)
            )
        );
    }

    public void jobCompletedNoCasesEvent(ScheduledTaskEventConfiguration eventConfig) {
        telemetryService.trackEvent(
            eventConfig.getJobCompletedEvent(),
            Map.of(
                SCHEDULER_NAME, eventConfig.getSchedulerName(),
                TOTAL_CASES, ZERO,
                SUCCEEDED_CASES, ZERO,
                FAILED_CASES, ZERO
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
                SCHEDULER_NAME, eventConfig.getSchedulerName(),
                TOTAL_CASES, String.valueOf(totalCases),
                SUCCEEDED_CASES, String.valueOf(succeededCases),
                FAILED_CASES, String.valueOf(failedCases),
                ABORT_REASON, reason != null ? reason : UNKNOWN
            )
        );
    }

    public void jobAbortedEvent(ScheduledTaskEventConfiguration eventConfig, String reason) {
        telemetryService.trackEvent(
            eventConfig.getJobAbortedEvent(),
            Map.of(
                SCHEDULER_NAME, eventConfig.getSchedulerName(),
                TOTAL_CASES, ZERO,
                SUCCEEDED_CASES, ZERO,
                FAILED_CASES, ZERO,
                ABORT_REASON, reason != null ? reason : UNKNOWN
            )
        );
    }
}
