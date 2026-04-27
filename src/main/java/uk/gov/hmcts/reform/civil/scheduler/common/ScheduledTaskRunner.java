package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ScheduledTaskRunner {

    private final ScheduledEventTracker eventTracker;

    @Value("${scheduler.circuitBreakerThreshold:5}")
    private int circuitBreakerThreshold;

    public void run(ScheduledTaskEventConfiguration eventConfig,
                    Supplier<Set<CaseDetails>> caseDetailsSupplier,
                    Consumer<CaseDetails> scheduledTask) {

        List<Long> failedCases = new ArrayList<>();
        int consecutiveFailures = 0;
        boolean abortedEarly = false;
        String abortReason = "";

        Set<CaseDetails> cases = caseDetailsSupplier.get();
        eventTracker.jobStartedEvent(eventConfig, cases.size());

        for (CaseDetails caseDetails : cases) {
            try {
                scheduledTask.accept(caseDetails);
                eventTracker.caseProcessedEvent(eventConfig, caseDetails.getId());
                consecutiveFailures = 0;
            } catch (Exception e) {
                failedCases.add(caseDetails.getId());
                eventTracker.caseFailedEvent(eventConfig, caseDetails, e);
                consecutiveFailures++;

                if (consecutiveFailures >= circuitBreakerThreshold) {
                    abortedEarly = true;
                    abortReason = e.getMessage();
                    break;
                }
            }
        }

        if (abortedEarly) {
            eventTracker.jobAbortedEvent(eventConfig, cases, failedCases, abortReason);
        }
        eventTracker.jobCompletedEvent(eventConfig, cases, failedCases, abortedEarly);
    }
}
