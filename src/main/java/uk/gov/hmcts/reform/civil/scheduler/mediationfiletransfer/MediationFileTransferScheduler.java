package uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.MediationSearchService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.mediation-file-transfer", name = "enabled", havingValue = "true")
public class MediationFileTransferScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "GenerateCsvAndSendToMmt";

    private final MediationSearchService searchService;
    private final ScheduledTaskRunner<CaseData, Long> scheduledTaskRunner;
    private final MediationFileTransferScheduledTask task;
    private final FeatureToggleService featureToggleService;

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
        scheduledTaskRunner.run(new ScheduledTaskEventConfiguration(SCHEDULER_NAME + "_CSV"), searchService.getInMediationCsv(), task);
        scheduledTaskRunner.run(new ScheduledTaskEventConfiguration(SCHEDULER_NAME + "_JSON"), searchService.getInMediationJson(), task);
    }
}
