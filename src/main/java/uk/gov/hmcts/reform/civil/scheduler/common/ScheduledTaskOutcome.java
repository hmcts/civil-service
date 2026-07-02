package uk.gov.hmcts.reform.civil.scheduler.common;

import java.util.List;

public record ScheduledTaskOutcome<I>(
    List<I> succeededCases,
    List<I> failedCases,
    boolean abortedEarly,
    String abortReason
) {}
