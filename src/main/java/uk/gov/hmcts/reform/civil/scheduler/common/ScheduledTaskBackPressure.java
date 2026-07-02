package uk.gov.hmcts.reform.civil.scheduler.common;

import java.time.Duration;

class ScheduledTaskBackPressure {

    private final ScheduledTaskBackPressureConfiguration configuration;
    private Duration currentDelay;

    ScheduledTaskBackPressure(ScheduledTaskBackPressureConfiguration configuration) {
        this.configuration = configuration != null
            ? configuration
            : ScheduledTaskBackPressureConfiguration.disabled();
        this.currentDelay = this.configuration.initialDelay();
    }

    Duration currentDelay() {
        return currentDelay;
    }

    void afterSuccess(Duration processingTime) {
        if (isSlow(processingTime)) {
            increaseDelay(configuration.delayIncreaseOnSlowCase());
        } else {
            reduceDelay(configuration.delayReductionOnSuccess());
        }
    }

    void afterFailure() {
        increaseDelay(configuration.delayIncreaseOnFailure());
    }

    private boolean isSlow(Duration processingTime) {
        return !configuration.slowCaseThreshold().isZero()
            && processingTime.compareTo(configuration.slowCaseThreshold()) > 0;
    }

    private void increaseDelay(Duration increase) {
        currentDelay = min(configuration.maxDelay(), currentDelay.plus(increase));
    }

    private void reduceDelay(Duration reduction) {
        currentDelay = max(Duration.ZERO, currentDelay.minus(reduction));
    }

    private Duration min(Duration left, Duration right) {
        return left.compareTo(right) <= 0 ? left : right;
    }

    private Duration max(Duration left, Duration right) {
        return left.compareTo(right) >= 0 ? left : right;
    }
}
