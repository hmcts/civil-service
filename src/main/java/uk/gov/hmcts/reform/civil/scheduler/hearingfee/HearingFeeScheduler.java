package uk.gov.hmcts.reform.civil.scheduler.hearingfee;

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
import uk.gov.hmcts.reform.civil.service.search.HearingFeeDueSearchService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.hearingFee", name = "enabled", havingValue = "true")
public class HearingFeeScheduler implements CivilScheduler {

    private static final String SCHEDULER_NAME = "HearingFee";

    private final HearingFeeDueSearchService searchService;
    private final ScheduledTaskRunner scheduledTaskRunner;
    private final HearingFeeSchedulerTask hearingFeeSchedulerTask;
    private final FeatureToggleService featureToggleService;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.hearingFee.cronExpression}")
    @SchedulerLock(name = "HearingFeeScheduler_check",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        if (featureToggleService.isSpringSchedulerEnabled()) {
            log.info("Running {} scheduler", SCHEDULER_NAME);
            scheduledTaskRunner.run(
                new ScheduledTaskEventConfiguration(SCHEDULER_NAME),
                searchService.getElasticSearchResult(),
                hearingFeeSchedulerTask
            );
        }
    }
}
