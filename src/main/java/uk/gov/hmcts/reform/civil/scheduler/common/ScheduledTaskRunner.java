package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskRunner {

    private final ScheduledEventTracker eventTracker;
    private final ScheduledTaskProcessor scheduledTaskProcessor;

    public void run(ScheduledTaskEventConfiguration eventConfig,
                    Supplier<Stream<CaseDetails>> caseDetailsSupplier,
                    Consumer<CaseDetails> scheduledTask) {

        final Stream<CaseDetails> cases;
        try {
            cases = Optional.ofNullable(caseDetailsSupplier.get()).orElse(Stream.empty());
            processCaseDetails(eventConfig, scheduledTask, cases);
        } catch (Exception e) {
            eventTracker.jobAbortedEvent(eventConfig, e.getMessage());
            log.error(
                "Scheduled task aborted due to error during case retrieval: {}",
                eventConfig.getSchedulerName(),
                e
            );
        }
    }

    private void processCaseDetails(ScheduledTaskEventConfiguration eventConfig, Consumer<CaseDetails> scheduledTask, Stream<CaseDetails> cases) {
        eventTracker.jobStartedEvent(eventConfig);
        log.info("Running scheduled task: {}", eventConfig.getSchedulerName());

        ScheduledTaskOutcome outcome = scheduledTaskProcessor.performProcessing(eventConfig, scheduledTask, cases);

        if (outcome.abortedEarly()) {
            eventTracker.jobAbortedEvent(
                eventConfig,
                outcome.succeededCases().size(),
                outcome.failedCases().size(),
                outcome.abortReason()
            );
            log.info(
                "Scheduled task aborted: {}, succeededCases: {}, failedCases: {}, abortReason: {}",
                eventConfig.getSchedulerName(),
                outcome.succeededCases().size(),
                outcome.failedCases().size(),
                outcome.abortReason()
            );
        } else {
            eventTracker.jobCompletedEvent(eventConfig, outcome.succeededCases().size(), outcome.failedCases().size());
            log.info(
                "Scheduled task completed: {}, succeededCases: {}, failedCases: {}",
                eventConfig.getSchedulerName(),
                outcome.succeededCases().size(),
                outcome.failedCases().size()
            );
        }
    }
}
