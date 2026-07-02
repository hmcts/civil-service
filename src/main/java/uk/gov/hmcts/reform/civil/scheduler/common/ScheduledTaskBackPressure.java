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
        currentDelay = currentDelay.plus(increase);
        if (currentDelay.compareTo(configuration.maxDelay()) > 0) {
            currentDelay = configuration.maxDelay();
        }
    }

    private void reduceDelay(Duration reduction) {
        currentDelay = currentDelay.minus(reduction);
        if (currentDelay.compareTo(configuration.initialDelay()) < 0) {
            currentDelay = configuration.initialDelay();
        }
    }
}
