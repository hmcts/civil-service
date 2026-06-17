package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.function.Consumer;

/**
 * Runner for scheduled tasks that coordinates between event tracking, searching and processing.
 * This component is prototype-scoped to ensure isolated state per scheduler instance.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskRunner {

    private final ScheduledEventTracker eventTracker;
    private final ScheduledTaskProcessor scheduledTaskProcessor;

    /**
     * Executes the scheduled task for cases found in the search result.
     * Handles null search results and empty search results by logging and tracking events appropriately.
     *
     * @param eventConfig   the event configuration
     * @param searchResult  the result of the elastic search
     * @param scheduledTask the task to be performed on each case
     */
    public void run(ScheduledTaskEventConfiguration eventConfig,
                    ElasticSearchResult searchResult,
                    Consumer<CaseDetails> scheduledTask) {

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

        processCaseDetails(eventConfig, scheduledTask, searchResult);
    }

    /**
     * Orchestrates the processing of case details by delegating to {@link ScheduledTaskProcessor}.
     * Tracks the start, completion, or early abortion of the job.
     *
     * @param eventConfig   the event configuration
     * @param scheduledTask the task to be performed on each case
     * @param searchResult  the result of the elastic search containing the stream of cases
     */
    private void processCaseDetails(ScheduledTaskEventConfiguration eventConfig,
                                    Consumer<CaseDetails> scheduledTask,
                                    ElasticSearchResult searchResult) {
        int totalCases = searchResult.totalResults();
        eventTracker.jobStartedEvent(eventConfig, totalCases);
        log.info("Running scheduled task: {}, totalCases: {}", eventConfig.getSchedulerName(), totalCases);

        ScheduledTaskOutcome outcome = scheduledTaskProcessor.performProcessing(
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
                outcome.abortReason()
            );
            log.info(
                "Scheduled task aborted: {}, totalCases: {}, succeededCases: {}, failedCases: {}, abortReason: {}",
                eventConfig.getSchedulerName(),
                totalCases,
                outcome.succeededCases().size(),
                outcome.failedCases().size(),
                outcome.abortReason()
            );
        } else {
            eventTracker.jobCompletedEvent(
                eventConfig,
                totalCases,
                outcome.succeededCases().size(),
                outcome.failedCases().size()
            );
            log.info(
                "Scheduled task completed: {}, totalCases: {}, succeededCases: {}, failedCases: {}",
                eventConfig.getSchedulerName(),
                totalCases,
                outcome.succeededCases().size(),
                outcome.failedCases().size()
            );
        }
    }
}
