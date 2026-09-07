package uk.gov.hmcts.reform.civil.scheduler.common;

import java.util.function.Consumer;

public interface ScheduledTask<T, I> extends Consumer<T> {

    I getItemId(T item);

    default long maxCasesPerRun() {
        return Long.MAX_VALUE;
    }

    default ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
        return ScheduledTaskBackPressureConfiguration.disabled();
    }
}
