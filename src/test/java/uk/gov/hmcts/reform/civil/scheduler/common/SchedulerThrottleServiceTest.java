package uk.gov.hmcts.reform.civil.scheduler.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulerThrottleServiceTest {

    private static final long LOCK_DURATION = 600000L;

    private EventProperties eventProperties;
    private SchedulerThrottleService service;

    @BeforeEach
    void setUp() {
        eventProperties = new EventProperties();
        eventProperties.setDispatchDelay(0);
        eventProperties.setLockDuration(LOCK_DURATION);
        service = new SchedulerThrottleService(eventProperties);
    }

    @Test
    void shouldNotThrottleWhenBatchSizeIsOne() {
        service.throttle(1);

        assertThat(Thread.currentThread().isInterrupted()).isFalse();
    }

    @Test
    void shouldRestoreInterruptedFlagWhenThrottleIsInterrupted() {
        eventProperties.setDispatchDelay(2000);

        try {
            Thread.currentThread().interrupt();

            service.throttle(26);

            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }
}
