package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;

@Service
@RequiredArgsConstructor
public class SchedulerThrottleService {

    private static final int SMALL_BATCH = 25;

    private final EventProperties eventProperties;

    public void throttle(long count) {
        throttle(count, eventProperties.getDispatchDelay());
    }

    public void throttle(long count, long delay) {
        throttle(count, delay, eventProperties.getLockDuration());
    }

    @SuppressWarnings("java:S2142")
    public void throttle(long count, long delay, long lock) {
        long effectiveDelay = calculateEffectiveDelay(count, lock, delay);
        if (effectiveDelay == 0) {
            return;
        }
        try {
            Thread.sleep(effectiveDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Calculates the effective delay for a batch processing task based on the total found items,
     * the lock duration, and the desired delay. Ensures the delay does not surpass the maximum
     * permissible delay derived from the lock duration and batch size.
     *
     * @param count the total number of items found in the batch.
     * @param lock  the duration for which the task is locked in milliseconds.
     * @param delay the desired delay in milliseconds between task executions.
     * @return the calculated effective delay in milliseconds.
     */
    private long calculateEffectiveDelay(long count, long lock, long delay) {
        if (count <= 1 || delay <= 0 || lock <= 0) {
            return 0;
        }

        if (count <= SMALL_BATCH && delay < 2000L) {
            return 0;
        }

        long maxExecutionTimeMs = (long) (lock * 0.8);
        long maxDelay = maxExecutionTimeMs / count;
        return Math.min(maxDelay, delay);
    }
}
