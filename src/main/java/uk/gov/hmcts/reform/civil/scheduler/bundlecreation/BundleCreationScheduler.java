package uk.gov.hmcts.reform.civil.scheduler.bundlecreation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ElasticSearchSchedulerRunner;
import uk.gov.hmcts.reform.civil.service.search.BundleCreationTriggerService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.bundle-creation", name = "enabled", havingValue = "true")
public class BundleCreationScheduler implements CivilScheduler {

    private static final String SCHEDULER_NAME = "BundleCreation";

    private final BundleCreationTriggerService searchService;
    private final ElasticSearchSchedulerRunner elasticSearchSchedulerRunner;
    private final BundleCreationScheduledTask bundleCreationScheduledTask;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.bundle-creation.cronExpression}")
    @SchedulerLock(name = "BundleCreationScheduler_createBundles",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        elasticSearchSchedulerRunner.run(
            SCHEDULER_NAME,
            searchService::getElasticSearchResult,
            bundleCreationScheduledTask
        );
    }
}
