package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskProcessor<T, I> {

    private final ScheduledEventTracker eventTracker;

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
        List<I> succeededItems = new ArrayList<>();
        List<I> failedItems = new ArrayList<>();
        int[] consecutiveFailures = new int[1];
        String[] abortReason = new String[1];
        ScheduledTaskBackPressure backPressure = new ScheduledTaskBackPressure(
            scheduledTask.backPressureConfiguration()
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
                succeededItems,
                failedItems,
                consecutiveFailures,
                abortReason
            ));

            return new ScheduledTaskOutcome<>(succeededItems, failedItems, !completed, abortReason[0]);
        } catch (ScheduledTaskInterruptedException e) {
            abortReason[0] = e.getMessage();
            return new ScheduledTaskOutcome<>(succeededItems, failedItems, true, abortReason[0]);
        }
    }

    private boolean processItem(ScheduledTaskEventConfiguration eventConfig,
                                       ScheduledTask<T, I> scheduledTask,
                                       T item,
                                       ScheduledTaskBackPressure backPressure,
                                       List<I> succeededItems,
                                       List<I> failedItems,
                                       int[] consecutiveFailures,
                                       String[] abortReason) {
        applyBackPressure(backPressure);

        I itemId = scheduledTask.getItemId(item);
        Instant startedAt = Instant.now();

        try {
            scheduledTask.accept(item);
            backPressure.afterSuccess(Duration.between(startedAt, Instant.now()));
            eventTracker.caseProcessedEvent(eventConfig, itemId.toString());
            succeededItems.add(itemId);
            consecutiveFailures[0] = 0;
        } catch (Exception e) {
            backPressure.afterFailure();
            failedItems.add(itemId);
            eventTracker.caseFailedEvent(eventConfig, itemId.toString(), e);
            log.error("Error processing item {}: {}", itemId, e.getMessage(), e);
            int failures = ++consecutiveFailures[0];

            if (failures >= circuitBreakerThreshold) {
                abortReason[0] = Objects.toString(e.getMessage(), e.getClass().getSimpleName());
                return false;
            }
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

    private void applyBackPressure(ScheduledTaskBackPressure backPressure) {
        Duration delay = backPressure.currentDelay();
        if (delay.isZero()) {
            return;
        }

        try {
            log.debug("Applying scheduled task backpressure delay: {}", delay);
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
}
