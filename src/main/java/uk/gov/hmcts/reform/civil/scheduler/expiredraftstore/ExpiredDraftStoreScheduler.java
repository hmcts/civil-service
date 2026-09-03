package uk.gov.hmcts.reform.civil.scheduler.expiredraftstore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledEventTracker;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiredDraftStoreScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "ExpiredDraftStore";

    private final FeatureToggleService featureToggleService;
    private final DraftStoreRepository draftStoreRepository;
    private final ScheduledEventTracker eventTracker;

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
    @Transactional
    @Override
    public void runScheduledTask() {
        if (!featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)) {
            return;
        }
        ScheduledTaskEventConfiguration config = new ScheduledTaskEventConfiguration(SCHEDULER_NAME);
        eventTracker.jobStartedEvent(config);

        try {
            log.info("Running {} scheduler", SCHEDULER_NAME);
            long deleted = draftStoreRepository.deleteByExpiresAtBefore(OffsetDateTime.now());
            log.info("{} deleted {} expired draft(s)", SCHEDULER_NAME, deleted);

            eventTracker.jobCompletedBulkEvent(config, (int) deleted);
        } catch (Exception e) {
            log.error("Error executing {} scheduler: {}", SCHEDULER_NAME, e.getMessage(), e);
            eventTracker.jobAbortedEvent(config, e.getMessage());
            throw e;
        }
    }
}
