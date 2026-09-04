package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Manages the execution of a sequence of {@link SchedulerInterceptor}s.
 * This class is stateful per execution and should be instantiated via {@link InterceptorRegistry}.
 *
 * @param <T> the type of item being processed
 */
@Slf4j
public class InterceptorChain<T> {

    private final List<SchedulerInterceptor<T>> interceptors;
    private final Consumer<InterceptorContext<T>> finalTask;
    private final Function<String, StopWatch> stopWatchFactory;
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
        this(interceptors, finalTask, StopWatch::new);
    }

    /**
     * Creates a new interceptor chain with a custom StopWatch factory.
     *
     * @param interceptors      the list of interceptors to execute
     * @param finalTask         the task to execute at the end of the chain
     * @param stopWatchFactory  the factory for StopWatch instances
     */
    public InterceptorChain(List<SchedulerInterceptor<T>> interceptors,
                            Consumer<InterceptorContext<T>> finalTask,
                            Function<String, StopWatch> stopWatchFactory) {
        this.interceptors = interceptors;
        this.finalTask = finalTask;
        this.stopWatchFactory = stopWatchFactory;
    }

    /**
     * Proceeds to the next interceptor in the chain.
     *
     * @param context the interceptor context
     */
    public void next(InterceptorContext<T> context) {
        if (index < interceptors.size()) {
            SchedulerInterceptor<T> interceptor = interceptors.get(index++);
            if (log.isDebugEnabled()) {
                log.debug("Executing interceptor: {}", interceptor.getClass().getSimpleName());
            }

            String taskName = interceptor.getClass().getSimpleName();
            long beforeDownstream = totalDownstreamTimeNanos;
            StopWatch stopWatch = stopWatchFactory.apply(taskName);
            stopWatch.start();
            try {
                interceptor.accept(context, this);
            } finally {
                stopWatch.stop();
                long durationNanos = stopWatch.getTotalTimeNanos();
                long exclusiveTimeNanos = durationNanos - (totalDownstreamTimeNanos - beforeDownstream);
                context.recordMetric(taskName, exclusiveTimeNanos / 1_000_000);
                totalDownstreamTimeNanos += exclusiveTimeNanos;
            }
        } else if (index == interceptors.size()) {
            index++;
            taskExecuted = true;
            StopWatch stopWatch = stopWatchFactory.apply("FinalTask");
            stopWatch.start();
            try {
                finalTask.accept(context);
            } finally {
                stopWatch.stop();
                long durationNanos = stopWatch.getTotalTimeNanos();
                context.recordMetric("FinalTask", durationNanos / 1_000_000);
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
}
