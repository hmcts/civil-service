package uk.gov.hmcts.reform.civil.scheduler.bundlecreation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.BundleCreationTriggerService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.bundleCreation", name = "enabled", havingValue = "true")
public class BundleCreationScheduler implements CivilScheduler {

    private static final String SCHEDULER_NAME = "BundleCreation";

    private final BundleCreationTriggerService searchService;
    private final ScheduledTaskRunner scheduledTaskRunner;
    private final BundleCreationScheduledTask bundleCreationScheduledTask;
    private final FeatureToggleService featureToggleService;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.bundleCreation.cronExpression}")
    @SchedulerLock(name = "BundleCreationScheduler_createBundles",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        if (featureToggleService.isSpringSchedulerEnabled()) {
            log.info("Running {} scheduler", SCHEDULER_NAME);
            ElasticSearchResult searchResult = searchService.getElasticSearchResult();
            int totalCases = searchResult == null ? 0 : searchResult.totalResults();
            scheduledTaskRunner.run(
                new ScheduledTaskEventConfiguration(SCHEDULER_NAME),
                searchResult,
                caseDetails -> bundleCreationScheduledTask.accept(caseDetails, totalCases)
            );
        }
    }
}
