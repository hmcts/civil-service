package uk.gov.hmcts.reform.civil.scheduler.evidenceupload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.EvidenceUploadNotificationSearchService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.evidenceUpload", name = "enabled", havingValue = "true")
public class EvidenceUploadScheduler implements CivilScheduler {

    private static final String SCHEDULER_NAME = "EvidenceUpload";

    private final EvidenceUploadNotificationSearchService searchService;
    private final ScheduledTaskRunner scheduledTaskRunner;
    private final EvidenceUploadSchedulerTask evidenceUploadSchedulerTask;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.evidenceUpload.cronExpression}")
    @SchedulerLock(name = "EvidenceUploadScheduler_notification",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        log.info("Running {} scheduler", SCHEDULER_NAME);
        scheduledTaskRunner.run(
            new ScheduledTaskEventConfiguration(SCHEDULER_NAME),
            searchService.getElasticSearchResult(),
            evidenceUploadSchedulerTask
        );
    }
}
