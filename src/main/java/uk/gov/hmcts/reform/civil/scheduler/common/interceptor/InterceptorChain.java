package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

/**
 * Manages the execution of a sequence of {@link SchedulerInterceptor}s.
 * This class is stateful per execution and should be instantiated via {@link InterceptorChainFactory}.
 *
 * @param <T> the type of item being processed
 */
@Slf4j
public class InterceptorChain<T> {

    private static final long NANOS_PER_MILLISECOND = 1_000_000L;

    private final List<SchedulerInterceptor<T>> interceptors;
    private final Consumer<InterceptorContext<T>> finalTask;
    private final LongSupplier nanoTimeSupplier;
    private int index = 0;
    private boolean taskExecuted = false;
    private long totalDownstreamTimeNanos = 0;

    /**
     * Creates a new interceptor chain.
     *
     * @param interceptors the list of interceptors to execute
     * @param finalTask    the task to execute at the end of the chain
     */
    public InterceptorChain(List<SchedulerInterceptor<T>> interceptors, Consumer<InterceptorContext<T>> finalTask) {
        this(interceptors, finalTask, System::nanoTime);
    }

    /**
     * Creates a new interceptor chain with a custom nanosecond time supplier.
     *
     * @param interceptors      the list of interceptors to execute
     * @param finalTask         the task to execute at the end of the chain
     * @param nanoTimeSupplier  the supplier of the current time in nanoseconds
     */
    public InterceptorChain(List<SchedulerInterceptor<T>> interceptors,
                            Consumer<InterceptorContext<T>> finalTask,
                            LongSupplier nanoTimeSupplier) {
        this.interceptors = interceptors;
        this.finalTask = finalTask;
        this.nanoTimeSupplier = nanoTimeSupplier;
    }

    /**
     * Proceeds to the next interceptor in the chain.
     *
     * @param context the interceptor context
     */
    public void next(InterceptorContext<T> context) {
        if (index < interceptors.size()) {
            SchedulerInterceptor<T> interceptor = interceptors.get(index++);
            String taskName = interceptor.getClass().getSimpleName();
            long beforeDownstream = totalDownstreamTimeNanos;

            long startNanos = nanoTimeSupplier.getAsLong();

            try {
                interceptor.accept(context, this);
            } finally {
                long durationNanos = nanoTimeSupplier.getAsLong() - startNanos;
                long exclusiveTimeNanos = durationNanos - (totalDownstreamTimeNanos - beforeDownstream);
                context.recordMetric(taskName, exclusiveTimeNanos / NANOS_PER_MILLISECOND);
                totalDownstreamTimeNanos += exclusiveTimeNanos;
            }
        } else if (index == interceptors.size()) {
            index++;
            taskExecuted = true;

            long startNanos = nanoTimeSupplier.getAsLong();

            try {
                finalTask.accept(context);
            } finally {
                long durationNanos = nanoTimeSupplier.getAsLong() - startNanos;
                context.recordMetric("FinalTask", durationNanos / NANOS_PER_MILLISECOND);
                totalDownstreamTimeNanos += durationNanos;
            }
        }
    }

    /**
     * Returns whether the final task was executed.
     *
     * @return true if the final task was executed, false otherwise
     */
    public boolean wasTaskExecuted() {
        return taskExecuted;
    }

    /**
     * Returns the total time taken by all interceptors and the final task in the chain.
     *
     * @return the total time in nanoseconds
     */
    public long getTotalTimeNanos() {
        return totalDownstreamTimeNanos;
    }
}
