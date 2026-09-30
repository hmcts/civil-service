package uk.gov.hmcts.reform.civil.scheduler.expiredraftstore;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledEventTracker;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.OffsetDateTime;

@Component
@Profile("!contract-test")
@Slf4j
public class ExpiredDraftStoreScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "ExpiredDraftStore";

    private final FeatureToggleService featureToggleService;
    private final ExpiredDraftStoreTask expiredDraftStoreTask;
    private final ScheduledEventTracker eventTracker;

    @Setter
    private int batchSize;

    public ExpiredDraftStoreScheduler(
        FeatureToggleService featureToggleService,
        ExpiredDraftStoreTask expiredDraftStoreTask,
        ScheduledEventTracker eventTracker,
        @Value("${scheduler.expired-draft-store.batchSize:500}") int batchSize
    ) {
        this.featureToggleService = featureToggleService;
        this.expiredDraftStoreTask = expiredDraftStoreTask;
        this.eventTracker = eventTracker;
        this.batchSize = batchSize;
    }

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.expired-draft-store.cronExpression}")
    @SchedulerLock(
        name = "ExpiredDraftStoreScheduler_purge",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}"
    )
    @Override
    public void runScheduledTask() {
        if (!featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)) {
            return;
        }
        ScheduledTaskEventConfiguration config = new ScheduledTaskEventConfiguration(SCHEDULER_NAME);
        eventTracker.jobStartedEvent(config);

        try {
            log.info("Running {} scheduler", SCHEDULER_NAME);
            long totalDeleted = 0;
            OffsetDateTime now = OffsetDateTime.now();
            int effectiveBatchSize = Math.max(1, batchSize);
            Pageable pageRequest = PageRequest.of(0, effectiveBatchSize);

            int deletedInBatch;
            do {
                deletedInBatch = expiredDraftStoreTask.deleteExpiredBatch(now, pageRequest);
                totalDeleted += deletedInBatch;
                if (deletedInBatch > 0) {
                    log.debug("{} deleted {} expired draft(s) in batch", SCHEDULER_NAME, deletedInBatch);
                }
            } while (deletedInBatch >= effectiveBatchSize);

            log.info("{} deleted {} expired draft(s)", SCHEDULER_NAME, totalDeleted);
            eventTracker.jobCompletedBulkEvent(config, (int) totalDeleted);
        } catch (Exception e) {
            log.error("Error executing {} scheduler: {}", SCHEDULER_NAME, e.getMessage(), e);
            eventTracker.jobAbortedEvent(config, e.getMessage());
            throw e;
        }
    }
}
