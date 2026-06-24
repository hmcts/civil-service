package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulerThrottleServiceTest {

    private static final long LOCK_DURATION = 600000L;

    @Test
    void shouldReturnZeroEffectiveDelayWhenCountIsOneOrLess() {
        assertThat(SchedulerThrottleService.calculateEffectiveDelay(1, LOCK_DURATION, 2000)).isZero();
        assertThat(SchedulerThrottleService.calculateEffectiveDelay(0, LOCK_DURATION, 2000)).isZero();
    }

    @Test
    void shouldReturnZeroEffectiveDelayWhenDelayOrLockIsNotPositive() {
        assertThat(SchedulerThrottleService.calculateEffectiveDelay(26, LOCK_DURATION, 0)).isZero();
        assertThat(SchedulerThrottleService.calculateEffectiveDelay(26, LOCK_DURATION, -1)).isZero();
        assertThat(SchedulerThrottleService.calculateEffectiveDelay(26, 0, 2000)).isZero();
        assertThat(SchedulerThrottleService.calculateEffectiveDelay(26, -1, 2000)).isZero();
    }

    @Test
    void shouldReturnZeroEffectiveDelayForSmallBatchWhenDelayIsBelowThreshold() {
        assertThat(SchedulerThrottleService.calculateEffectiveDelay(25, LOCK_DURATION, 1999)).isZero();
    }

    @Test
    void shouldUseDesiredDelayForSmallBatchWhenDelayMeetsThreshold() {
        assertThat(SchedulerThrottleService.calculateEffectiveDelay(25, LOCK_DURATION, 2000)).isEqualTo(2000);
    }

    @Test
    void shouldUseDesiredDelayWhenItIsLowerThanMaximumAllowedDelay() {
        assertThat(SchedulerThrottleService.calculateEffectiveDelay(100, LOCK_DURATION, 1000)).isEqualTo(1000);
    }

    @Test
    void shouldCapEffectiveDelayUsingLockDurationAndBatchSize() {
        assertThat(SchedulerThrottleService.calculateEffectiveDelay(10, 10000, 5000)).isEqualTo(800);
    }

    @Test
    void shouldNotThrottleWhenBatchSizeIsOne() {
        SchedulerThrottleService.throttle(1, 0, LOCK_DURATION);

        assertThat(Thread.currentThread().isInterrupted()).isFalse();
    }

    @Test
    void shouldRestoreInterruptedFlagWhenThrottleIsInterrupted() {
        try {
            Thread.currentThread().interrupt();

            SchedulerThrottleService.throttle(26, 2000, LOCK_DURATION);

            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }
}
