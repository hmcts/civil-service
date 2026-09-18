package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.SchedulerInterceptor;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.SchedulerInterceptorResolver;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import java.time.Duration;
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
    private final SchedulerInterceptorResolver interceptorResolver;

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
            StopWatch stopWatch = new StopWatch(config.getSchedulerName());

            stopWatch.start("search");
            TaskResult<T> searchResult = config.getSearchResultSupplier().get();
            stopWatch.stop();
            Duration searchDuration = Duration.ofNanos(stopWatch.getLastTaskTimeNanos());

            List<SchedulerInterceptor<T>> interceptors = interceptorResolver.resolveInterceptors(config);

            execute(new ScheduledTaskEventConfiguration(config.getSchedulerName()), searchResult, config.getScheduledTask(), interceptors, searchDuration, stopWatch);
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
     * @param stopWatch      the StopWatch for the job
     */
    private void execute(ScheduledTaskEventConfiguration eventConfig,
                         TaskResult<T> searchResult,
                         ScheduledTask<T, I> scheduledTask,
                         List<SchedulerInterceptor<T>> interceptors,
                         Duration searchDuration,
                         StopWatch stopWatch) {

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

        processItems(eventConfig, scheduledTask, searchResult, interceptors, searchDuration, stopWatch);
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
                              StopWatch stopWatch) {
        int totalCases = searchResult.totalResults();
        eventTracker.jobStartedEvent(eventConfig, totalCases);
        log.info("Running scheduled task: {}, totalCases: {}", eventConfig.getSchedulerName(), totalCases);

        stopWatch.start("process");
        ScheduledTaskOutcome<I> outcome = scheduledTaskProcessor.performProcessing(
            eventConfig,
            scheduledTask,
            searchResult,
            interceptors
        );
        stopWatch.stop();

        Duration totalDuration = Duration.ofNanos(stopWatch.getTotalTimeNanos());

        if (outcome.abortedEarly()) {
            eventTracker.jobAbortedEvent(
                eventConfig,
                ScheduledJobReport.builder()
                    .totalCases(totalCases)
                    .succeededCases(outcome.succeededCases().size())
                    .failedCases(outcome.failedCases().size())
                    .abortedCases(outcome.abortedCases().size())
                    .jobAbortReason(outcome.jobAbortReason())
                    .cumulativeDelay(outcome.cumulativeDelay())
                    .searchDuration(searchDuration)
                    .processingDuration(outcome.processingDuration())
                    .totalDuration(totalDuration)
                    .build()
            );
            log.info(
                "Scheduled task aborted: {}, totalCases: {}, succeededCases: {}, failedCases: {}, abortedCases: {}, jobAbortReason: {}, cumulativeDelay: {}, " +
                    "searchDuration: {}ms, processingDuration: {}ms, totalDuration: {}ms",
                eventConfig.getSchedulerName(),
                totalCases,
                outcome.succeededCases().size(),
                outcome.failedCases().size(),
                outcome.abortedCases().size(),
                outcome.jobAbortReason(),
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
                    .abortedCases(outcome.abortedCases().size())
                    .cumulativeDelay(outcome.cumulativeDelay())
                    .searchDuration(searchDuration)
                    .processingDuration(outcome.processingDuration())
                    .totalDuration(totalDuration)
                    .build()
            );
            log.info(
                "Scheduled task completed: {}, totalCases: {}, succeededCases: {}, failedCases: {}, abortedCases: {}, cumulativeDelay: {}, " +
                    "searchDuration: {}ms, processingDuration: {}ms, totalDuration: {}ms",
                eventConfig.getSchedulerName(),
                totalCases,
                outcome.succeededCases().size(),
                outcome.failedCases().size(),
                outcome.abortedCases().size(),
                outcome.cumulativeDelay().toMillis(),
                searchDuration.toMillis(),
                outcome.processingDuration().toMillis(),
                totalDuration.toMillis()
            );
        }
    }
}
