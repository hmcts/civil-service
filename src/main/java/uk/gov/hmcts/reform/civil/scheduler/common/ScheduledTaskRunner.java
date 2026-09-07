package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.SchedulerInterceptor;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
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
        run(ScheduledTaskConfiguration.<T, I>builder()
            .schedulerName(schedulerName)
            .searchResultSupplier(searchResultSupplier)
            .scheduledTask(scheduledTask)
            .build());
    }

    /**
     * Executes the scheduled task using the provided configuration object.
     * This is the preferred method for running scheduled tasks as it provides
     * a cleaner and more descriptive API.
     *
     * @param config the configuration for the scheduled task
     */
    public void run(ScheduledTaskConfiguration<T, I> config) {
        if (featureToggleService.isSpringSchedulerEnabled(config.getSchedulerName())) {
            log.info("Running {} scheduler", config.getSchedulerName());
            Instant start = Instant.now();
            TaskResult<T> searchResult = config.getSearchResultSupplier().get();
            Duration searchDuration = Duration.between(start, Instant.now());
            execute(new ScheduledTaskEventConfiguration(config.getSchedulerName()), searchResult, config.getScheduledTask(), config.getInterceptors(), searchDuration, start);
        }
    }

    /**
     * Executes the scheduled task for items found in the search result.
     * Handles null search results and empty search results by logging and tracking events appropriately.
     *
     * @param eventConfig    the event configuration
     * @param searchResult   the result of the search
     * @param scheduledTask  the task to be performed on each item
     * @param interceptors   the list of interceptors to apply
     * @param searchDuration the duration of the search
     * @param start          the start time of the job
     */
    private void execute(ScheduledTaskEventConfiguration eventConfig,
                         TaskResult<T> searchResult,
                         ScheduledTask<T, I> scheduledTask,
                         List<SchedulerInterceptor<T>> interceptors,
                         Duration searchDuration,
                         Instant start) {

        if (searchResult == null) {
            eventTracker.jobAbortedEvent(eventConfig, "SearchResult cannot be null", searchDuration);
            log.error(
                "Scheduled task aborted due to SearchResult being null: {}",
                eventConfig.getSchedulerName()
            );
            return;
        }

        if (searchResult.isEmpty()) {
            eventTracker.jobStartedEvent(eventConfig, 0);
            eventTracker.jobCompletedNoCasesEvent(eventConfig, searchDuration);
            log.info("Scheduled task completed: {}, totalCases: 0", eventConfig.getSchedulerName());
            return;
        }

        processItems(eventConfig, scheduledTask, searchResult, interceptors, searchDuration, start);
    }

    /**
     * Orchestrates the processing of items by delegating to {@link ScheduledTaskProcessor}.
     * Tracks the start, completion, or early abortion of the job.
     *
     * @param eventConfig   the event configuration
     * @param scheduledTask the task to be performed on each item
     * @param searchResult  the result of the search containing the stream of items
     * @param interceptors  the list of interceptors to apply
     */
    private void processItems(ScheduledTaskEventConfiguration eventConfig,
                              ScheduledTask<T, I> scheduledTask,
                              TaskResult<T> searchResult,
                              List<SchedulerInterceptor<T>> interceptors,
                              Duration searchDuration,
                              Instant start) {
        int totalCases = searchResult.totalResults();
        eventTracker.jobStartedEvent(eventConfig, totalCases);
        log.info("Running scheduled task: {}, totalCases: {}", eventConfig.getSchedulerName(), totalCases);

        ScheduledTaskOutcome<I> outcome = scheduledTaskProcessor.performProcessing(
            eventConfig,
            scheduledTask,
            searchResult,
            interceptors
        );

        Duration totalDuration = Duration.between(start, Instant.now());

        if (outcome.abortedEarly()) {
            eventTracker.jobAbortedEvent(
                eventConfig,
                ScheduledJobReport.builder()
                    .totalCases(totalCases)
                    .succeededCases(outcome.succeededCases().size())
                    .failedCases(outcome.failedCases().size())
                    .abortReason(outcome.abortReason())
                    .cumulativeDelay(outcome.cumulativeDelay())
                    .searchDuration(searchDuration)
                    .processingDuration(outcome.processingDuration())
                    .totalDuration(totalDuration)
                    .build()
            );
            log.info(
                "Scheduled task aborted: {}, totalCases: {}, succeededCases: {}, failedCases: {}, abortReason: {}, cumulativeDelay: {}, " +
                    "searchDuration: {}ms, processingDuration: {}ms, totalDuration: {}ms",
                eventConfig.getSchedulerName(),
                totalCases,
                outcome.succeededCases().size(),
                outcome.failedCases().size(),
                outcome.abortReason(),
                outcome.cumulativeDelay().toMillis(),
                searchDuration.toMillis(),
                outcome.processingDuration().toMillis(),
                totalDuration.toMillis()
            );
        } else {
            eventTracker.jobCompletedEvent(
                eventConfig,
                ScheduledJobReport.builder()
                    .totalCases(totalCases)
                    .succeededCases(outcome.succeededCases().size())
                    .failedCases(outcome.failedCases().size())
                    .cumulativeDelay(outcome.cumulativeDelay())
                    .searchDuration(searchDuration)
                    .processingDuration(outcome.processingDuration())
                    .totalDuration(totalDuration)
                    .build()
            );
            log.info(
                "Scheduled task completed: {}, totalCases: {}, succeededCases: {}, failedCases: {}, cumulativeDelay: {}, " +
                    "searchDuration: {}ms, processingDuration: {}ms, totalDuration: {}ms",
                eventConfig.getSchedulerName(),
                totalCases,
                outcome.succeededCases().size(),
                outcome.failedCases().size(),
                outcome.cumulativeDelay().toMillis(),
                searchDuration.toMillis(),
                outcome.processingDuration().toMillis(),
                totalDuration.toMillis()
            );
        }
    }
}
