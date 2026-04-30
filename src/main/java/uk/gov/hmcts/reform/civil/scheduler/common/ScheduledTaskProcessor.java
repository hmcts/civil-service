package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskProcessor {

    private final ScheduledEventTracker eventTracker;

    @Value("${scheduler.circuitBreakerThreshold:5}")
    private int circuitBreakerThreshold;

    public ScheduledTaskOutcome performProcessing(ScheduledTaskEventConfiguration eventConfig,
                                                 Consumer<CaseDetails> scheduledTask,
                                                 Set<CaseDetails> cases) {
        List<Long> succeededCases = new ArrayList<>();
        List<Long> failedCases = new ArrayList<>();
        int consecutiveFailures = 0;
        boolean abortedEarly = false;
        String abortReason = "";

        for (CaseDetails caseDetails : cases) {
            try {
                scheduledTask.accept(caseDetails);
                eventTracker.caseProcessedEvent(eventConfig, caseDetails.getId());
                succeededCases.add(caseDetails.getId());
                consecutiveFailures = 0;
            } catch (Exception e) {
                failedCases.add(caseDetails.getId());
                eventTracker.caseFailedEvent(eventConfig, caseDetails, e);
                log.error("Error processing case {}: {}", caseDetails.getId(), e.getMessage(), e);
                consecutiveFailures++;

                if (consecutiveFailures >= circuitBreakerThreshold) {
                    abortedEarly = true;
                    abortReason = e.getMessage();
                    break;
                }
            }
        }
        return new ScheduledTaskOutcome(succeededCases, failedCases, abortedEarly, abortReason);
    }
}
