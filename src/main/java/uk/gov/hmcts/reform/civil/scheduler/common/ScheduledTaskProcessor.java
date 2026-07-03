package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
                                                  ScheduledTask scheduledTask,
                                                  ElasticSearchResult searchResult) {
        List<Long> succeededCases = new ArrayList<>();
        List<Long> failedCases = new ArrayList<>();
        int[] consecutiveFailures = new int[1];
        String[] abortReason = new String[1];
        ScheduledTaskBackPressure backPressure = new ScheduledTaskBackPressure(
            scheduledTask.backPressureConfiguration()
        );

        Stream<CaseDetails> sequentialStream = searchResult.caseDetailsStream()
            .sequential()
            .limit(maxCasesPerRun(scheduledTask));

        try {
            boolean completed = sequentialStream.allMatch(caseDetails -> processCaseDetails(
                eventConfig,
                scheduledTask,
                caseDetails,
                backPressure,
                succeededCases,
                failedCases,
                consecutiveFailures,
                abortReason
            ));

            return new ScheduledTaskOutcome(succeededCases, failedCases, !completed, abortReason[0]);
        } catch (ScheduledTaskInterruptedException e) {
            abortReason[0] = e.getMessage();
            return new ScheduledTaskOutcome(succeededCases, failedCases, true, abortReason[0]);
        }
    }

    private boolean processCaseDetails(ScheduledTaskEventConfiguration eventConfig,
                                       ScheduledTask scheduledTask,
                                       CaseDetails caseDetails,
                                       ScheduledTaskBackPressure backPressure,
                                       List<Long> succeededCases,
                                       List<Long> failedCases,
                                       int[] consecutiveFailures,
                                       String[] abortReason) {
        applyBackPressure(backPressure);

        Long caseId = caseDetails.getId();
        Instant startedAt = Instant.now();

        try {
            scheduledTask.accept(caseDetails);
            backPressure.afterSuccess(Duration.between(startedAt, Instant.now()));
            eventTracker.caseProcessedEvent(eventConfig, caseId);
            succeededCases.add(caseId);
            consecutiveFailures[0] = 0;
        } catch (Exception e) {
            backPressure.afterFailure();
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
    }

    private long maxCasesPerRun(ScheduledTask scheduledTask) {
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
