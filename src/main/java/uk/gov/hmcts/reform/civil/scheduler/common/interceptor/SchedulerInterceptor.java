package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import org.springframework.core.Ordered;

/**
 * Interface for interceptors that can participate in the execution chain of a scheduled task.
 * Interceptors are ordered using Spring's {@link Ordered} interface.
 *
 * @param <T> the type of item being processed
 */
public interface SchedulerInterceptor<T> extends Ordered {

    /**
     * Intercepts the processing of an item.
     * Implementations must call {@code chain.next(context)} to continue the execution of the chain
     * or throw a {@link TaskAbortedException} to skip the item without marking it as a failure.
     *
     * @param context the interceptor context containing the item and configuration
     * @param chain   the interceptor chain
     */
    void accept(InterceptorContext<T> context, InterceptorChain<T> chain);

    /**
     * Determines if this interceptor supports the given scheduler.
     *
     * @param schedulerName the name of the scheduler
     * @return true if this interceptor should be included in the chain for the given scheduler
     */
    default boolean supports(String schedulerName) {
        return false;
    }

    /**
     * Returns the order value of this interceptor.
     *
     * <p>Interceptors are executed in ascending order of their priority value.
     * A lower value indicates a higher priority (executed earlier).
     * The default order is 0.
     *
     * @return the order value
     */
    @Override
    default int getOrder() {
        return 0;
    }
}
