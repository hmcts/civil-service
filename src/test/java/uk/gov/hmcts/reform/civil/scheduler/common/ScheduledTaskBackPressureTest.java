package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ScheduledTaskBackPressureTest {

    private static final String SCHEDULER_NAME = "JudgmentBuffer";

    @Mock
    private ScheduledEventTracker eventTracker;

    @Mock
    private ScheduledTaskEventConfiguration eventConfig;

    @Test
    void shouldInitializeWithInitialDelay() {
        ScheduledTaskBackPressureConfiguration config = new ScheduledTaskBackPressureConfiguration(
            Duration.ofMillis(100),
            Duration.ofMillis(500),
            Duration.ofMillis(50),
            Duration.ofMillis(25),
            Duration.ofMillis(10),
            Duration.ofMillis(200)
        );

        ScheduledTaskBackPressure backPressure = new ScheduledTaskBackPressure(SCHEDULER_NAME, config, eventTracker, eventConfig);

        assertThat(backPressure.currentDelay()).isEqualTo(Duration.ofMillis(100));
    }

    @Test
    void shouldIncreaseDelayOnFailure_UpToMaxDelay() {
        ScheduledTaskBackPressureConfiguration config = new ScheduledTaskBackPressureConfiguration(
            Duration.ofMillis(100),
            Duration.ofMillis(150),
            Duration.ofMillis(40),
            Duration.ZERO,
            Duration.ZERO,
            Duration.ZERO
        );

        ScheduledTaskBackPressure backPressure = new ScheduledTaskBackPressure(SCHEDULER_NAME, config, eventTracker, eventConfig);

        backPressure.afterFailure();
        assertThat(backPressure.currentDelay()).isEqualTo(Duration.ofMillis(140));

        backPressure.afterFailure();
        assertThat(backPressure.currentDelay()).isEqualTo(Duration.ofMillis(150)); // Capped at max
    }

    @Test
    void shouldIncreaseDelayOnSlowCase() {
        ScheduledTaskBackPressureConfiguration config = new ScheduledTaskBackPressureConfiguration(
            Duration.ofMillis(100),
            Duration.ofMillis(500),
            Duration.ZERO,
            Duration.ofMillis(50),
            Duration.ZERO,
            Duration.ofMillis(200)
        );

        ScheduledTaskBackPressure backPressure = new ScheduledTaskBackPressure(SCHEDULER_NAME, config, eventTracker, eventConfig);

        // Slow case: 201ms > 200ms threshold
        backPressure.afterSuccess(Duration.ofMillis(201));
        assertThat(backPressure.currentDelay()).isEqualTo(Duration.ofMillis(150));
    }

    @Test
    void shouldReduceDelayOnSuccess_ButNotBelowInitialDelay() {
        ScheduledTaskBackPressureConfiguration config = new ScheduledTaskBackPressureConfiguration(
            Duration.ofMillis(100),
            Duration.ofMillis(500),
            Duration.ofMillis(50),
            Duration.ZERO,
            Duration.ofMillis(20),
            Duration.ofMillis(200)
        );

        ScheduledTaskBackPressure backPressure = new ScheduledTaskBackPressure(SCHEDULER_NAME, config, eventTracker, eventConfig);

        // Increase delay first
        backPressure.afterFailure(); // 100 + 50 = 150
        assertThat(backPressure.currentDelay()).isEqualTo(Duration.ofMillis(150));

        // Reduce delay: Fast success (100ms < 200ms)
        backPressure.afterSuccess(Duration.ofMillis(100));
        assertThat(backPressure.currentDelay()).isEqualTo(Duration.ofMillis(130)); // 150 - 20 = 130

        backPressure.afterSuccess(Duration.ofMillis(100));
        assertThat(backPressure.currentDelay()).isEqualTo(Duration.ofMillis(110)); // 130 - 20 = 110

        backPressure.afterSuccess(Duration.ofMillis(100));
        assertThat(backPressure.currentDelay()).isEqualTo(Duration.ofMillis(100)); // 110 - 20 = 90, but capped at 100 (initial)

        backPressure.afterSuccess(Duration.ofMillis(100));
        assertThat(backPressure.currentDelay()).isEqualTo(Duration.ofMillis(100)); // Still capped at 100
    }

    @Test
    void shouldWorkWithZeroInitialDelay() {
        ScheduledTaskBackPressureConfiguration config = new ScheduledTaskBackPressureConfiguration(
            Duration.ZERO,
            Duration.ofMillis(100),
            Duration.ofMillis(50),
            Duration.ZERO,
            Duration.ofMillis(20),
            Duration.ofMillis(200)
        );

        ScheduledTaskBackPressure backPressure = new ScheduledTaskBackPressure(SCHEDULER_NAME, config, eventTracker, eventConfig);

        backPressure.afterFailure(); // 0 + 50 = 50
        backPressure.afterSuccess(Duration.ofMillis(100)); // 50 - 20 = 30
        backPressure.afterSuccess(Duration.ofMillis(100)); // 30 - 20 = 10
        backPressure.afterSuccess(Duration.ofMillis(100)); // 10 - 20 = -10, capped at 0

        assertThat(backPressure.currentDelay()).isEqualTo(Duration.ZERO);
    }

    @Test
    void shouldFireTelemetryEvent_whenDelayChanges() {
        ScheduledTaskBackPressureConfiguration config = new ScheduledTaskBackPressureConfiguration(
            Duration.ofMillis(100),
            Duration.ofMillis(500),
            Duration.ofMillis(50),
            Duration.ZERO,
            Duration.ofMillis(20),
            Duration.ofMillis(200)
        );

        ScheduledTaskBackPressure backPressure = new ScheduledTaskBackPressure(SCHEDULER_NAME, config, eventTracker, eventConfig);

        // Increase delay
        backPressure.afterFailure();
        verify(eventTracker).backPressureUpdatedEvent(eventConfig, Duration.ofMillis(100), Duration.ofMillis(150));

        // Decrease delay
        backPressure.afterSuccess(Duration.ofMillis(100));
        verify(eventTracker).backPressureUpdatedEvent(eventConfig, Duration.ofMillis(150), Duration.ofMillis(130));
    }

    @Test
    void shouldNotFireTelemetryEvent_whenDelayDoesNotChange() {
        ScheduledTaskBackPressureConfiguration config = new ScheduledTaskBackPressureConfiguration(
            Duration.ofMillis(100),
            Duration.ofMillis(100), // Max is same as initial
            Duration.ofMillis(50),
            Duration.ZERO,
            Duration.ZERO,
            Duration.ZERO
        );

        ScheduledTaskBackPressure backPressure = new ScheduledTaskBackPressure(SCHEDULER_NAME, config, eventTracker, eventConfig);

        backPressure.afterFailure(); // Already at max
        assertThat(backPressure.currentDelay()).isEqualTo(Duration.ofMillis(100));
        verifyNoInteractions(eventTracker);
    }
}
