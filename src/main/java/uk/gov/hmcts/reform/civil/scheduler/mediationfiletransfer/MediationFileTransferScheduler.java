package uk.gov.hmcts.reform.civil.scheduler.mediationfiletransfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.mediation.MediationFileTransferService;
import uk.gov.hmcts.reform.civil.service.search.MediationSearchService;

import java.util.List;

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
    private final MediationFileTransferService mediationFileTransferService;

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
        scheduledTaskRunner.run(SCHEDULER_NAME, this::getCsvCasesAfterTransfer, task);
        scheduledTaskRunner.run(SCHEDULER_NAME, this::getJsonCasesAfterTransfer, task);
    }

    private TaskResult<CaseData> getCsvCasesAfterTransfer() {
        TaskResult<CaseData> result = searchService.getInMediationCsv();
        List<CaseData> cases = result.itemStream().toList();
        mediationFileTransferService.sendCsv(cases);
        return new ListTaskResult<>(cases, result.totalResults());
    }

    private TaskResult<CaseData> getJsonCasesAfterTransfer() {
        TaskResult<CaseData> result = searchService.getInMediationJson();
        List<CaseData> cases = result.itemStream().toList();
        mediationFileTransferService.sendJson(cases);
        return new ListTaskResult<>(cases, result.totalResults());
    }
}
