package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.InterceptorChain;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.InterceptorContext;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.InterceptorRegistry;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.TaskAbortedException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskProcessor<T, I> {

    private final ScheduledEventTracker eventTracker;
    private final InterceptorRegistry interceptorRegistry;

    @Value("${scheduler.circuitBreakerThreshold:5}")
    private int circuitBreakerThreshold;

    /**
     * Performs processing of scheduled tasks for a stream of items.
     * Use allMatch to short-circuit the stream if the circuit breaker is triggered.
     * Once the predicate returns false, allMatch stops processing further elements.
     *
     * @param eventConfig   the event configuration
     * @param scheduledTask the task to be performed on each item
     * @param searchResult  the result of the search containing the stream of items
     * @return the outcome of the scheduled task processing
     */
    public ScheduledTaskOutcome<I> performProcessing(ScheduledTaskEventConfiguration eventConfig,
                                                     ScheduledTask<T, I> scheduledTask,
                                                     TaskResult<T> searchResult) {
        ProcessingContext context = new ProcessingContext();
        ScheduledTaskBackPressure backPressure = new ScheduledTaskBackPressure(
            eventConfig.getSchedulerName(),
            scheduledTask.backPressureConfiguration(),
            eventTracker,
            eventConfig
        );

        Stream<T> sequentialStream = searchResult.itemStream()
            .sequential()
            .limit(maxCasesPerRun(scheduledTask));

        try {
            boolean completed = sequentialStream.allMatch(item -> processItem(
                eventConfig,
                scheduledTask,
                item,
                backPressure,
                context
            ));

            return new ScheduledTaskOutcome<>(
                context.succeededItems,
                context.failedItems,
                !completed,
                context.abortReason.get(),
                Duration.ofMillis(context.cumulativeDelayMillis.get())
            );
        } catch (ScheduledTaskInterruptedException e) {
            context.abortReason.set(e.getMessage());
            return new ScheduledTaskOutcome<>(
                context.succeededItems,
                context.failedItems,
                true,
                context.abortReason.get(),
                Duration.ofMillis(context.cumulativeDelayMillis.get())
            );
        }
    }

    private boolean processItem(ScheduledTaskEventConfiguration eventConfig,
                                ScheduledTask<T, I> scheduledTask,
                                T item,
                                ScheduledTaskBackPressure backPressure,
                                ProcessingContext context) {
        applyBackPressure(backPressure, context);

        I itemId = scheduledTask.getItemId(item);
        Instant startedAt = Instant.now();
        InterceptorContext<T> interceptorContext = new InterceptorContext<>(eventConfig.getSchedulerName(), item);

        try {
            InterceptorChain<T> chain = interceptorRegistry.buildChain(eventConfig.getSchedulerName(), scheduledTask);
            chain.next(interceptorContext);

            if (chain.wasTaskExecuted()) {
                handleSuccess(eventConfig, itemId, startedAt, backPressure, context, interceptorContext);
            } else {
                handleAbortion(eventConfig, itemId, "Silent abortion", interceptorContext);
            }
        } catch (TaskAbortedException e) {
            handleAbortion(eventConfig, itemId, e.getReason(), interceptorContext);
        } catch (Exception e) {
            return handleFailure(eventConfig, itemId, e, backPressure, context, interceptorContext);
        }
        return true;
    }

    private void handleSuccess(ScheduledTaskEventConfiguration eventConfig,
                               I itemId,
                               Instant startedAt,
                               ScheduledTaskBackPressure backPressure,
                               ProcessingContext context,
                               InterceptorContext<T> interceptorContext) {
        backPressure.afterSuccess(Duration.between(startedAt, Instant.now()));
        eventTracker.caseProcessedEvent(eventConfig, itemId.toString(), interceptorContext.getMetrics());
        context.succeededItems.add(itemId);
        context.consecutiveFailures.set(0);
    }

    private void handleAbortion(ScheduledTaskEventConfiguration eventConfig,
                                I itemId,
                                String reason,
                                InterceptorContext<T> interceptorContext) {
        log.info("Scheduled task: {}, ItemId: {}, Aborted: {}",
                 eventConfig.getSchedulerName(), itemId, reason);
        eventTracker.caseAbortedEvent(eventConfig, itemId.toString(), reason, interceptorContext.getMetrics());
    }

    private boolean handleFailure(ScheduledTaskEventConfiguration eventConfig,
                                  I itemId,
                                  Exception e,
                                  ScheduledTaskBackPressure backPressure,
                                  ProcessingContext context,
                                  InterceptorContext<T> interceptorContext) {
        backPressure.afterFailure();
        context.failedItems.add(itemId);
        eventTracker.caseFailedEvent(eventConfig, itemId.toString(), e, interceptorContext.getMetrics());
        log.error("Error processing item {}: {}", itemId, e.getMessage(), e);
        int failures = context.consecutiveFailures.incrementAndGet();

        if (failures >= circuitBreakerThreshold) {
            context.abortReason.set(Objects.toString(e.getMessage(), e.getClass().getSimpleName()));
            return false;
        }
        return true;
    }

    private long maxCasesPerRun(ScheduledTask<T, I> scheduledTask) {
        long maxCasesPerRun = scheduledTask.maxCasesPerRun();
        if (maxCasesPerRun < 0) {
            throw new IllegalArgumentException("maxCasesPerRun cannot be negative");
        }
        return maxCasesPerRun;
    }

    private void applyBackPressure(ScheduledTaskBackPressure backPressure, ProcessingContext context) {
        Duration delay = backPressure.currentDelay();
        if (delay.isZero()) {
            return;
        }

        try {
            log.debug("Applying scheduled task backpressure delay: {}", delay);
            context.cumulativeDelayMillis.addAndGet(delay.toMillis());
            sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ScheduledTaskInterruptedException(
                "Scheduled task interrupted while applying backpressure",
                e
            );
        }
    }

    void sleep(Duration delay) throws InterruptedException {
        Thread.sleep(delay.toMillis());
    }

    @RequiredArgsConstructor
    private class ProcessingContext {
        private final List<I> succeededItems = new ArrayList<>();
        private final List<I> failedItems = new ArrayList<>();
        private final AtomicInteger consecutiveFailures = new AtomicInteger();
        private final AtomicReference<String> abortReason = new AtomicReference<>();
        private final AtomicLong cumulativeDelayMillis = new AtomicLong();
    }
}
