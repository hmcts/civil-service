package uk.gov.hmcts.reform.civil.scheduler.common;

import java.time.Duration;
import java.util.List;

public record ScheduledTaskOutcome<I>(
    List<I> succeededCases,
    List<I> failedCases,
    List<I> abortedCases,
    boolean abortedEarly,
    String jobAbortReason,
    Duration cumulativeDelay,
    Duration processingDuration
) {}
