package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory that sorts {@link SchedulerInterceptor}s and builds the execution chain for a specific task.
 */
@Slf4j
@Service
public class InterceptorChainFactory {

    /**
     * Sorts the provided list of interceptors based on their order.
     *
     * @param interceptors the list of interceptors to sort
     * @param <T>          the type of item being processed
     * @return a new sorted list of interceptors
     */
    public <T> List<SchedulerInterceptor<T>> sortInterceptors(List<SchedulerInterceptor<T>> interceptors) {
        List<SchedulerInterceptor<T>> sortedInterceptors = new ArrayList<>(interceptors);
        AnnotationAwareOrderComparator.sort(sortedInterceptors);
        return sortedInterceptors;
    }

    /**
     * Builds an interceptor chain for a specific task using a pre-sorted list of interceptors.
     *
     * @param scheduledTask      the task to be executed at the end of the chain
     * @param sortedInterceptors the sorted list of interceptors
     * @param <T>                the type of item being processed
     * @param <I>                the type of the item ID
     * @return a new {@link InterceptorChain} ready for execution
     */
    public <T, I> InterceptorChain<T> buildChain(ScheduledTask<T, I> scheduledTask,
                                                 List<SchedulerInterceptor<T>> sortedInterceptors) {
        return new InterceptorChain<>(sortedInterceptors, context -> scheduledTask.accept(context.getItem()));
    }
}
