package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskRunner {

    private final ScheduledEventTracker eventTracker;
    private final ScheduledTaskProcessor scheduledTaskProcessor;

    public void run(ScheduledTaskEventConfiguration eventConfig,
                    Supplier<Set<CaseDetails>> caseDetailsSupplier,
                    Consumer<CaseDetails> scheduledTask) {

        final Set<CaseDetails> cases;
        try {
            cases = Optional.ofNullable(caseDetailsSupplier.get())
                .orElse(Collections.emptySet());
        } catch (Exception e) {
            eventTracker.jobAbortedEvent(eventConfig, e.getMessage());
            log.error("Scheduled task aborted due to error during case retrieval: {}", eventConfig.getSchedulerName(), e);
            return;
        }

        if (cases.isEmpty()) {
            eventTracker.jobStartedEvent(eventConfig, 0);
            eventTracker.jobCompletedNoCasesEvent(eventConfig);
            log.info("Scheduled task completed: {}, totalCases: 0", eventConfig.getSchedulerName());
        } else {
            processCaseDetails(eventConfig, scheduledTask, cases);
        }
    }

    private void processCaseDetails(ScheduledTaskEventConfiguration eventConfig, Consumer<CaseDetails> scheduledTask, Set<CaseDetails> cases) {
        eventTracker.jobStartedEvent(eventConfig, cases.size());
        log.info("Running scheduled task: {}, totalCases: {}", eventConfig.getSchedulerName(), cases.size());

        ScheduledTaskOutcome outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, cases);

        if (outcome.abortedEarly()) {
            eventTracker.jobAbortedEvent(eventConfig, cases.size(), outcome.succeededCases().size(), outcome.failedCases().size(), outcome.abortReason());
            log.info(
                "Scheduled task aborted: {}, totalCases: {}, succeededCases: {}, failedCases: {}, abortReason: {}",
                eventConfig.getSchedulerName(),
                cases.size(),
                outcome.succeededCases().size(),
                outcome.failedCases().size(),
                outcome.abortReason()
            );
        } else {
            eventTracker.jobCompletedEvent(eventConfig, cases.size(), outcome.succeededCases().size(), outcome.failedCases().size());
            log.info(
                "Scheduled task completed: {}, totalCases: {}, succeededCases: {}, failedCases: {}",
                eventConfig.getSchedulerName(),
                cases.size(),
                outcome.succeededCases().size(),
                outcome.failedCases().size()
            );
        }
    }
}
