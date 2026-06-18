package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskProcessor {

    private final ScheduledEventTracker eventTracker;

    @Value("${scheduler.circuitBreakerThreshold:5}")
    private int circuitBreakerThreshold;

    /**
     * Performs processing of scheduled tasks for a stream of cases.
     * Use allMatch to short-circuit the stream if the circuit breaker is triggered.
     * Once the predicate returns false, allMatch stops processing further elements.
     *
     * @param eventConfig   the event configuration
     * @param scheduledTask the task to be performed on each case
     * @param searchResult  the result of the elastic search containing the stream of cases
     * @return the outcome of the scheduled task processing
     */
    public ScheduledTaskOutcome performProcessing(ScheduledTaskEventConfiguration eventConfig,
                                                  Consumer<CaseDetails> scheduledTask,
                                                  ElasticSearchResult searchResult) {
        List<Long> succeededCases = new ArrayList<>();
        List<Long> failedCases = new ArrayList<>();
        int[] consecutiveFailures = new int[1];
        String[] abortReason = new String[1];
        Stream<CaseDetails> sequentialStream = searchResult.caseDetailsStream().sequential();

        boolean completed = sequentialStream.allMatch(caseDetails -> {
            Long caseId = caseDetails.getId();
            try {
                scheduledTask.accept(caseDetails);
                eventTracker.caseProcessedEvent(eventConfig, caseId);
                succeededCases.add(caseId);
                consecutiveFailures[0] = 0;
            } catch (Exception e) {
                failedCases.add(caseId);
                eventTracker.caseFailedEvent(eventConfig, caseId, e);
                log.error("Error processing case {}: {}", caseId, e.getMessage(), e);
                int failures = ++consecutiveFailures[0];

                if (failures >= circuitBreakerThreshold) {
                    abortReason[0] = Objects.toString(e.getMessage(), e.getClass().getSimpleName());
                    return false;
                }
            }
            return true;
        });

        return new ScheduledTaskOutcome(succeededCases, failedCases, !completed, abortReason[0]);
    }
}
