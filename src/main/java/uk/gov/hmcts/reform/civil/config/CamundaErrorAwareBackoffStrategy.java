package uk.gov.hmcts.reform.civil.config;

import org.camunda.bpm.client.backoff.ErrorAwareBackoffStrategy;
import org.camunda.bpm.client.backoff.ExponentialErrorBackoffStrategy;
import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.task.ExternalTask;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Exponential backoff for the external task client that only backs off when a
 * {@code fetchAndLock} call actually fails.
 *
 * <p>The plain {@link org.camunda.bpm.client.backoff.ExponentialBackoffStrategy} also ramps on an
 * empty poll, which forces a trade-off: a short cap keeps idle task-pickup latency low but barely
 * throttles an upstream outage, a long cap throttles the outage but delays pickup after a quiet
 * period. Because civil-service long-polls ({@code asyncResponseTimeout} 29.5s) an empty poll
 * already takes ~29.5s to return, so it needs no extra backoff at all.
 *
 * <p>This strategy therefore stays at {@code 0ms} on every successful or empty poll and only ramps
 * on error - a gateway 502/503/504, a dropped connection, or an unparseable body (EXC-CS-020) -
 * where {@code error} is non-null. That lets the cap be set high enough to genuinely throttle a
 * retry storm without ever affecting healthy task pickup. The level resets to zero on the first
 * poll that does not carry an error.
 *
 * <p>Equivalent to {@link ExponentialErrorBackoffStrategy} but with an {@link AtomicInteger} level
 * so it is safe if the client ever acquires on more than one thread, as the
 * {@link org.camunda.bpm.client.backoff.BackoffStrategy} contract recommends.
 */
public class CamundaErrorAwareBackoffStrategy implements ErrorAwareBackoffStrategy {

    private final long initTime;
    private final float factor;
    private final long maxTime;
    private final AtomicInteger level = new AtomicInteger(0);

    /**
     * Creates a strategy with the given exponential parameters.
     *
     * @param initTime wait in milliseconds after the first failed {@code fetchAndLock}
     * @param factor   base of the power by which the wait grows on each consecutive failure
     * @param maxTime  maximum wait in milliseconds between {@code fetchAndLock} attempts
     */
    public CamundaErrorAwareBackoffStrategy(long initTime, float factor, long maxTime) {
        this.initTime = initTime;
        this.factor = factor;
        this.maxTime = maxTime;
    }

    @Override
    public void reconfigure(List<ExternalTask> externalTasks, ExternalTaskClientException error) {
        if (error != null) {
            level.incrementAndGet();
        } else {
            level.set(0);
        }
    }

    @Override
    public long calculateBackoffTime() {
        int currentLevel = level.get();
        if (currentLevel == 0) {
            return 0L;
        }

        long backoffTime = (long) (initTime * Math.pow(factor, (double) currentLevel - 1));
        return Math.min(backoffTime, maxTime);
    }
}
