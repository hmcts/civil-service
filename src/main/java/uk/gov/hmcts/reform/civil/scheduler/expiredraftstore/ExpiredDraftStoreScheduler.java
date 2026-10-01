package uk.gov.hmcts.reform.civil.scheduler.expiredraftstore;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.scheduler.common.TaskResult;
import uk.gov.hmcts.reform.draftstore.repositories.DraftStoreRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Profile("!contract-test")
@Slf4j
public class ExpiredDraftStoreScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "ExpiredDraftStore";

    private final DraftStoreRepository draftStoreRepository;
    private final ExpiredDraftStoreTask expiredDraftStoreTask;
    private final ScheduledTaskRunner<List<UUID>, String> scheduledTaskRunner;

    @Setter
    private int batchSize;

    @Setter
    private int maxBatchesPerRun;

    public ExpiredDraftStoreScheduler(
        DraftStoreRepository draftStoreRepository,
        ExpiredDraftStoreTask expiredDraftStoreTask,
        ScheduledTaskRunner<List<UUID>, String> scheduledTaskRunner,
        @Value("${scheduler.expired-draft-store.batchSize:500}") int batchSize,
        @Value("${scheduler.expired-draft-store.maxBatchesPerRun:20}") int maxBatchesPerRun
    ) {
        this.draftStoreRepository = draftStoreRepository;
        this.expiredDraftStoreTask = expiredDraftStoreTask;
        this.scheduledTaskRunner = scheduledTaskRunner;
        this.batchSize = batchSize;
        this.maxBatchesPerRun = maxBatchesPerRun;
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
        scheduledTaskRunner.run(
            ScheduledTaskConfiguration.<List<UUID>, String>builder()
                .schedulerName(SCHEDULER_NAME)
                .searchResultSupplier(this::getExpiredBatches)
                .scheduledTask(expiredDraftStoreTask)
                .useDefaultInterceptors(false)
                .build()
        );
    }

    private TaskResult<List<UUID>> getExpiredBatches() {
        OffsetDateTime now = OffsetDateTime.now();
        int effectiveBatchSize = Math.max(1, batchSize);
        int effectiveMaxBatches = Math.max(1, maxBatchesPerRun);

        List<List<UUID>> batches = new ArrayList<>();
        int page = 0;
        List<UUID> pageIds;

        do {
            pageIds = draftStoreRepository.findExpiredIds(now, PageRequest.of(page, effectiveBatchSize));
            if (!pageIds.isEmpty()) {
                batches.add(pageIds);
                page++;
            }
        } while (pageIds.size() == effectiveBatchSize && page < effectiveMaxBatches);

        return new ListTaskResult<>(batches);
    }
}
