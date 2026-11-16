package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

/**
 * Marker interface for interceptors that are applied automatically to every compatible scheduled task
 * (when {@code useDefaultInterceptors} is enabled).
 * Interceptors that only implement {@link SchedulerInterceptor} are custom and must be supplied explicitly
 * through the task configuration.
 *
 * @param <T> the type of item being processed
 */
public interface DefaultSchedulerInterceptor<T> extends SchedulerInterceptor<T> {
}
