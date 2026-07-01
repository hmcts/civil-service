package uk.gov.hmcts.reform.civil.scheduler.takecaseoffline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ElasticSearchSchedulerRunner;
import uk.gov.hmcts.reform.civil.service.search.TakeCaseOfflineSearchService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.take-case-offline", name = "enabled", havingValue = "true")
public class TakeCaseOfflineScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "TakeCaseOffline";

    private final TakeCaseOfflineSearchService searchService;
    private final ElasticSearchSchedulerRunner elasticSearchSchedulerRunner;
    private final TakeCaseOfflineScheduledTask takeCaseOfflineScheduledTask;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.take-case-offline.cronExpression}")
    @SchedulerLock(name = "TakeCaseOfflineScheduler_takeCasesOffline",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        elasticSearchSchedulerRunner.run(
            SCHEDULER_NAME,
            searchService::getElasticSearchResult,
            takeCaseOfflineScheduledTask
        );
    }
}
