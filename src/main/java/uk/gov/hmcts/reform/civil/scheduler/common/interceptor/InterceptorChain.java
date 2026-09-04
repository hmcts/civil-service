package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.function.Consumer;

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
    private int index = 0;
    private boolean taskExecuted = false;

    /**
     * Creates a new interceptor chain.
     *
     * @param interceptors the list of interceptors to execute
     * @param finalTask    the task to execute at the end of the chain
     */
    public InterceptorChain(List<SchedulerInterceptor<T>> interceptors, Consumer<InterceptorContext<T>> finalTask) {
        this.interceptors = interceptors;
        this.finalTask = finalTask;
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
            interceptor.accept(context, this);
        } else if (index == interceptors.size()) {
            index++;
            taskExecuted = true;
            finalTask.accept(context);
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
