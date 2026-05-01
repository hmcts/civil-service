package uk.gov.hmcts.reform.civil.scheduler.common;

import java.util.List;

public record ScheduledTaskOutcome(
    List<Long> succeededCases,
    List<Long> failedCases,
    boolean abortedEarly,
    String abortReason
) {}
