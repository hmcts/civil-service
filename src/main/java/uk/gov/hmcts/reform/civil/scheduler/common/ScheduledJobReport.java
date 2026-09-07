package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.Builder;
import java.time.Duration;

@Builder
public record ScheduledJobReport(
    int totalCases,
    int succeededCases,
    int failedCases,
    Duration cumulativeDelay,
    Duration searchDuration,
    Duration processingDuration,
    Duration totalDuration,
    String abortReason
) {
    public ScheduledJobReport {
        if (cumulativeDelay == null) {
            cumulativeDelay = Duration.ZERO;
        }
        if (searchDuration == null) {
            searchDuration = Duration.ZERO;
        }
        if (processingDuration == null) {
            processingDuration = Duration.ZERO;
        }
        if (totalDuration == null) {
            totalDuration = Duration.ZERO;
        }
    }
}
