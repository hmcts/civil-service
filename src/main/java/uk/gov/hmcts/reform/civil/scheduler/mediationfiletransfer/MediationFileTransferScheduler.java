package uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledEventTracker;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.mediation-file-transfer", name = "enabled", havingValue = "true")
public class MediationFileTransferScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "GenerateCsvAndSendToMmt";

    private final MediationFileTransferScheduledTask scheduledTask;
    private final ScheduledEventTracker eventTracker;
    private final FeatureToggleService featureToggleService;

    @Value("${scheduler.circuitBreakerThreshold:5}")
    private int circuitBreakerThreshold;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.mediation-file-transfer.cronExpression}")
    @SchedulerLock(name = "MediationFileTransferScheduler_generateAndTransfer",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        if (!featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)) {
            return;
        }

        log.info("Running {} scheduler", SCHEDULER_NAME);
        ScheduledTaskEventConfiguration eventConfig = new ScheduledTaskEventConfiguration(SCHEDULER_NAME);
        MediationFileTransferResult csvResult = scheduledTask.generateCsvAndTransfer(circuitBreakerThreshold);
        MediationFileTransferResult jsonResult = scheduledTask.generateJsonAndTransfer(circuitBreakerThreshold);
        trackOutcome(eventConfig, List.of(csvResult, jsonResult));
    }

    private void trackOutcome(ScheduledTaskEventConfiguration eventConfig, List<MediationFileTransferResult> results) {
        int totalCases = results.stream().mapToInt(MediationFileTransferResult::totalCases).sum();

        if (totalCases == 0) {
            eventTracker.jobStartedEvent(eventConfig, 0);
            eventTracker.jobCompletedNoCasesEvent(eventConfig);
            return;
        }

        eventTracker.jobStartedEvent(eventConfig, totalCases);

        results.forEach(result -> {
            result.succeededCaseIds().forEach(caseId -> eventTracker.caseProcessedEvent(eventConfig, caseId));
            result.failedCases().forEach(failedCase ->
                eventTracker.caseFailedEvent(eventConfig, failedCase.caseId(), failedCase.exception()));
        });

        int succeededCases = results.stream().mapToInt(result -> result.succeededCaseIds().size()).sum();
        int failedCases = results.stream().mapToInt(result -> result.failedCases().size()).sum();
        MediationFileTransferResult abortedResult = results.stream()
            .filter(MediationFileTransferResult::abortedEarly)
            .findFirst()
            .orElse(null);

        if (abortedResult == null) {
            eventTracker.jobCompletedEvent(eventConfig, totalCases, succeededCases, failedCases);
        } else {
            eventTracker.jobAbortedEvent(
                eventConfig,
                totalCases,
                succeededCases,
                failedCases,
                abortedResult.abortReason()
            );
        }
    }
}
