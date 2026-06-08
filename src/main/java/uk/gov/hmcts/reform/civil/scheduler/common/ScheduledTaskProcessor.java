package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskProcessor {

    private final ScheduledEventTracker eventTracker;

    @Value("${scheduler.circuitBreakerThreshold:5}")
    private int circuitBreakerThreshold;

    public ScheduledTaskOutcome performProcessing(ScheduledTaskEventConfiguration eventConfig,
                                                  Consumer<CaseDetails> scheduledTask,
                                                  ElasticSearchResult searchResult) {
        List<Long> succeededCases = Collections.synchronizedList(new ArrayList<>());
        List<Long> failedCases = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger consecutiveFailures = new AtomicInteger(0);
        StringBuilder abortReason = new StringBuilder();
        Stream<CaseDetails> sequentialStream = searchResult.caseDetailsStream().sequential();

        boolean completed = sequentialStream.allMatch(caseDetails -> {
            Long caseId = caseDetails.getId();
            try {
                scheduledTask.accept(caseDetails);
                eventTracker.caseProcessedEvent(eventConfig, caseId);
                succeededCases.add(caseId);
                consecutiveFailures.set(0);
            } catch (Exception e) {
                failedCases.add(caseId);
                eventTracker.caseFailedEvent(eventConfig, caseId, e);
                log.error("Error processing case {}: {}", caseId, e.getMessage(), e);
                int failures = consecutiveFailures.incrementAndGet();

                if (failures >= circuitBreakerThreshold) {
                    abortReason.append(Objects.toString(e.getMessage(), e.getClass().getSimpleName()));
                    return false;
                }
            }
            return true;
        });

        return new ScheduledTaskOutcome(succeededCases, failedCases, !completed, abortReason.toString());
    }
}
