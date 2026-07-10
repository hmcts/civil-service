package uk.gov.hmcts.reform.civil.scheduler.common;

import java.time.Duration;
import java.util.Objects;

public record ScheduledTaskBackPressureConfiguration(
    Duration initialDelay,
    Duration maxDelay,
    Duration delayIncreaseOnFailure,
    Duration delayIncreaseOnSlowCase,
    Duration delayReductionOnSuccess,
    Duration slowCaseThreshold
) {

    private static final Duration ZERO = Duration.ZERO;

    public ScheduledTaskBackPressureConfiguration {
        requireNonNegative(initialDelay, "initialDelay");
        requireNonNegative(maxDelay, "maxDelay");
        requireNonNegative(delayIncreaseOnFailure, "delayIncreaseOnFailure");
        requireNonNegative(delayIncreaseOnSlowCase, "delayIncreaseOnSlowCase");
        requireNonNegative(delayReductionOnSuccess, "delayReductionOnSuccess");
        requireNonNegative(slowCaseThreshold, "slowCaseThreshold");

        if (maxDelay.compareTo(initialDelay) < 0) {
            throw new IllegalArgumentException("maxDelay must be greater than or equal to initialDelay");
        }
    }

    public static ScheduledTaskBackPressureConfiguration disabled() {
        return new ScheduledTaskBackPressureConfiguration(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
    }

    private static void requireNonNegative(Duration duration, String fieldName) {
        Objects.requireNonNull(duration, fieldName + " cannot be null");
        if (duration.isNegative()) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
    }
}
