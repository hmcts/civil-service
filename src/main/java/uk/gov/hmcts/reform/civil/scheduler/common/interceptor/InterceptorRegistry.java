package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry that manages all {@link SchedulerInterceptor}s and builds the execution chain for a specific task.
 * It caches the filtered list of interceptors for each scheduler to optimize performance.
 */
@Slf4j
@Service
public class InterceptorRegistry {

    private final List<SchedulerInterceptor<?>> interceptors;
    private final Map<String, List<SchedulerInterceptor<?>>> cachedInterceptors = new ConcurrentHashMap<>();

    public InterceptorRegistry(List<SchedulerInterceptor<?>> interceptors) {
        this.interceptors = new ArrayList<>(interceptors);
        AnnotationAwareOrderComparator.sort(this.interceptors);
    }

    /**
     * Builds an interceptor chain for a specific scheduler and task.
     * The chain will include all interceptors that support the scheduler, sorted by priority,
     * followed by the final execution of the task itself.
     *
     * @param schedulerName the name of the scheduler
     * @param scheduledTask the task to be executed at the end of the chain
     * @param <T>           the type of item being processed
     * @param <I>           the type of the item ID
     * @return a new {@link InterceptorChain} ready for execution
     */
    @SuppressWarnings("unchecked")
    public <T, I> InterceptorChain<T> buildChain(String schedulerName, ScheduledTask<T, I> scheduledTask) {
        List<SchedulerInterceptor<T>> filteredInterceptors = (List<SchedulerInterceptor<T>>) (List<?>) cachedInterceptors.computeIfAbsent(
            schedulerName,
            name -> {
                log.debug("Building interceptor list for scheduler: {}", name);
                return interceptors.stream()
                    .filter(interceptor -> interceptor.supports(name))
                    .collect(Collectors.toList());
            }
        );

        return new InterceptorChain<>(filteredInterceptors, context -> scheduledTask.accept(context.getItem()));
    }
}
