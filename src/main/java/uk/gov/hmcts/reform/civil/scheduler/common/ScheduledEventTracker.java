package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.service.TelemetryService;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public void caseFailedEvent(ScheduledTaskEventConfiguration eventConfig, CaseDetails caseDetails, Exception e) {
        telemetryService.trackEvent(
            eventConfig.getCaseFailedEvent(), Map.of(
                "schedulerName", eventConfig.getSchedulerName(),
                "caseId", String.valueOf(caseDetails.getId()),
                "status", "FAILURE",
                "error", e.getMessage(),
                "errorCategory", errorCategorizer.categorizeError(e)
            )
        );
    }

    public void jobCompletedEvent(ScheduledTaskEventConfiguration eventConfig,
                                  Set<CaseDetails> cases,
                                  List<Long> succeededCases,
                                  List<Long> failedCases) {
        telemetryService.trackEvent(
            eventConfig.getJobCompletedEvent(),
            Map.of(
                "schedulerName", eventConfig.getSchedulerName(),
                "totalCases", String.valueOf(cases.size()),
                "succeededCases", String.valueOf(succeededCases.size()),
                "failedCases", String.valueOf(failedCases.size())
            )
        );
    }

    public void jobAbortedEvent(ScheduledTaskEventConfiguration eventConfig,
                                Set<CaseDetails> cases,
                                List<Long> succeededCases,
                                List<Long> failedCases,
                                String reason) {
        telemetryService.trackEvent(
            eventConfig.getJobAbortedEvent(),
            Map.of(
                "schedulerName", eventConfig.getSchedulerName(),
                "totalCases", String.valueOf(cases.size()),
                "succeededCases", String.valueOf(succeededCases.size()),
                "failedCases", String.valueOf(failedCases.size()),
                "abortReason", reason != null ? reason : "Unknown"
            )
        );
    }
}
