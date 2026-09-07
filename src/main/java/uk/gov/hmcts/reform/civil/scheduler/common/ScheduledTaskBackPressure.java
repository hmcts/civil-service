package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
class ScheduledTaskBackPressure {

    private final String schedulerName;
    private final ScheduledTaskBackPressureConfiguration configuration;
    private final ScheduledEventTracker eventTracker;
    private final ScheduledTaskEventConfiguration eventConfig;
    private Duration currentDelay;

    ScheduledTaskBackPressure(String schedulerName,
                              ScheduledTaskBackPressureConfiguration configuration,
                              ScheduledEventTracker eventTracker,
                              ScheduledTaskEventConfiguration eventConfig) {
        this.schedulerName = schedulerName;
        this.configuration = configuration != null
            ? configuration
            : ScheduledTaskBackPressureConfiguration.disabled();
        this.eventTracker = eventTracker;
        this.eventConfig = eventConfig;
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
        Duration previousDelay = currentDelay;
        currentDelay = currentDelay.plus(increase);
        if (currentDelay.compareTo(configuration.maxDelay()) > 0) {
            currentDelay = configuration.maxDelay();
        }

        notifyIfChanged(previousDelay);
    }

    private void reduceDelay(Duration reduction) {
        Duration previousDelay = currentDelay;
        currentDelay = currentDelay.minus(reduction);
        if (currentDelay.compareTo(configuration.initialDelay()) < 0) {
            currentDelay = configuration.initialDelay();
        }

        notifyIfChanged(previousDelay);
    }

    private void notifyIfChanged(Duration previousDelay) {
        if (!currentDelay.equals(previousDelay)) {
            log.info("{} backpressure: delay changed from {}ms to {}ms",
                     schedulerName, previousDelay.toMillis(), currentDelay.toMillis());
            if (eventTracker != null && eventConfig != null) {
                eventTracker.backPressureUpdatedEvent(eventConfig, previousDelay, currentDelay);
            }
        }
    }
}
