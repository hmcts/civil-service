package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.SchedulerInterceptor;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Value
@Builder
public class ScheduledTaskConfiguration<T, I> {

    String schedulerName;
    Supplier<? extends TaskResult<T>> searchResultSupplier;
    ScheduledTask<T, I> scheduledTask;
    @Builder.Default
    List<SchedulerInterceptor<T>> interceptors = Collections.emptyList();
}
