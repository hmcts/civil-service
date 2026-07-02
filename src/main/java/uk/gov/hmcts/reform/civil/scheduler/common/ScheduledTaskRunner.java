package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.function.Supplier;

/**
 * Runner for scheduled tasks that coordinates between event tracking, searching and processing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskRunner<T, I> {

    private final ScheduledEventTracker eventTracker;
    private final ScheduledTaskProcessor<T, I> scheduledTaskProcessor;
    private final FeatureToggleService featureToggleService;

    /**
     * Executes the scheduled task if the feature toggle is enabled.
     * Handles searching, logging and processing of items.
     *
     * @param schedulerName         the name of the scheduler
     * @param searchResultSupplier  the supplier for search results
     * @param scheduledTask         the task to be performed on each item
     */
    public void run(String schedulerName,
                    Supplier<? extends TaskResult<T>> searchResultSupplier,
                    ScheduledTask<T, I> scheduledTask) {
        if (featureToggleService.isSpringSchedulerEnabled(schedulerName)) {
            log.info("Running {} scheduler", schedulerName);
            TaskResult<T> searchResult = searchResultSupplier.get();
            run(new ScheduledTaskEventConfiguration(schedulerName), searchResult, scheduledTask);
        }
    }

    /**
     * Executes the scheduled task for items found in the search result.
     * Handles null search results and empty search results by logging and tracking events appropriately.
     *
     * @param eventConfig   the event configuration
     * @param searchResult  the result of the search
     * @param scheduledTask the task to be performed on each item
     */
    public void run(ScheduledTaskEventConfiguration eventConfig,
                    TaskResult<T> searchResult,
                    ScheduledTask<T, I> scheduledTask) {

        if (searchResult == null) {
            eventTracker.jobAbortedEvent(eventConfig, "SearchResult cannot be null");
            log.error(
                "Scheduled task aborted due to SearchResult being null: {}",
                eventConfig.getSchedulerName()
            );
            return;
        }

        if (searchResult.isEmpty()) {
            eventTracker.jobStartedEvent(eventConfig, 0);
            eventTracker.jobCompletedNoCasesEvent(eventConfig);
            log.info("Scheduled task completed: {}, totalCases: 0", eventConfig.getSchedulerName());
            return;
        }

        processItems(eventConfig, scheduledTask, searchResult);
    }

    /**
     * Orchestrates the processing of items by delegating to {@link ScheduledTaskProcessor}.
     * Tracks the start, completion, or early abortion of the job.
     *
     * @param eventConfig   the event configuration
     * @param scheduledTask the task to be performed on each item
     * @param searchResult  the result of the search containing the stream of items
     */
    private void processItems(ScheduledTaskEventConfiguration eventConfig,
                                    ScheduledTask<T, I> scheduledTask,
                                    TaskResult<T> searchResult) {
        int totalCases = searchResult.totalResults();
        eventTracker.jobStartedEvent(eventConfig, totalCases);
        log.info("Running scheduled task: {}, totalCases: {}", eventConfig.getSchedulerName(), totalCases);

        ScheduledTaskOutcome<I> outcome = scheduledTaskProcessor.performProcessing(
            eventConfig,
            scheduledTask,
            searchResult
        );

        if (outcome.abortedEarly()) {
            eventTracker.jobAbortedEvent(
                eventConfig,
                totalCases,
                outcome.succeededCases().size(),
                outcome.failedCases().size(),
                outcome.abortReason(),
                outcome.cumulativeDelay()
            );
            log.info(
                "Scheduled task aborted: {}, totalCases: {}, succeededCases: {}, failedCases: {}, abortReason: {}, cumulativeDelay: {}",
                eventConfig.getSchedulerName(),
                totalCases,
                outcome.succeededCases().size(),
                outcome.failedCases().size(),
                outcome.abortReason(),
                outcome.cumulativeDelay()
            );
        } else {
            eventTracker.jobCompletedEvent(
                eventConfig,
                totalCases,
                outcome.succeededCases().size(),
                outcome.failedCases().size(),
                outcome.cumulativeDelay()
            );
            log.info(
                "Scheduled task completed: {}, totalCases: {}, succeededCases: {}, failedCases: {}, cumulativeDelay: {}",
                eventConfig.getSchedulerName(),
                totalCases,
                outcome.succeededCases().size(),
                outcome.failedCases().size(),
                outcome.cumulativeDelay()
            );
        }
    }
}
