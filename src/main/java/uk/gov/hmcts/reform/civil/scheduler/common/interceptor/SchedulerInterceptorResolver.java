package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolver for scheduled task interceptors that handles merging default and task-specific interceptors.
 */
@Component
@RequiredArgsConstructor
public class SchedulerInterceptorResolver {

    private final List<SchedulerInterceptor<?>> allInterceptors;

    /**
     * Resolves and merges interceptors for the given task configuration.
     * Includes default interceptors if enabled and compatible, and task-specific interceptors.
     *
     * @param config the task configuration
     * @param <T>    the item type
     * @param <I>    the item ID type
     * @return the resolved list of interceptors
     */
    @SuppressWarnings("unchecked")
    public <T, I> List<SchedulerInterceptor<T>> resolveInterceptors(ScheduledTaskConfiguration<T, I> config) {
        List<SchedulerInterceptor<T>> merged = new ArrayList<>();
        if (config.isUseDefaultInterceptors()) {
            allInterceptors.stream()
                .filter(interceptor -> isCompatible(interceptor, config.getScheduledTask()))
                .map(interceptor -> (SchedulerInterceptor<T>) interceptor)
                .forEach(merged::add);
        }

        for (SchedulerInterceptor<T> interceptor : config.getInterceptors()) {
            if (!merged.contains(interceptor)) {
                merged.add(interceptor);
            }
        }

        return merged;
    }

    private <T, I> boolean isCompatible(SchedulerInterceptor<?> interceptor, ScheduledTask<T, I> task) {
        ResolvableType interceptorType = ResolvableType.forClass(interceptor.getClass())
            .as(SchedulerInterceptor.class)
            .getGeneric(0);

        ResolvableType taskType = ResolvableType.forInstance(task)
            .as(ScheduledTask.class)
            .getGeneric(0);

        return interceptorType.isAssignableFrom(taskType);
    }
}
