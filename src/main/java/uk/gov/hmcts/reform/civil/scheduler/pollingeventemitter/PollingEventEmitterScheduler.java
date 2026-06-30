package uk.gov.hmcts.reform.civil.scheduler.pollingeventemitter;

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
import uk.gov.hmcts.reform.civil.service.search.CaseReadyBusinessProcessSearchService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.polling-event-emitter", name = "enabled", havingValue = "true")
public class PollingEventEmitterScheduler implements CivilScheduler {

    private static final String SCHEDULER_NAME = "PollingEventEmitter";
    static final int FIFTY_MINUTES_IN_SECONDS = 3000;

    private final CaseReadyBusinessProcessSearchService searchService;
    private final ScheduledTaskRunner scheduledTaskRunner;
    private final PollingEventEmitterScheduledTask pollingEventEmitterScheduledTask;
    private final FeatureToggleService featureToggleService;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.polling-event-emitter.cronExpression}")
    @SchedulerLock(name = "PollingEventEmitterScheduler_eventEmitter",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        if (featureToggleService.isSpringSchedulerEnabled(SCHEDULER_NAME)) {
            log.info("Running {} scheduler", SCHEDULER_NAME);
            ElasticSearchResult searchResult = searchService.getElasticSearchResult();

            scheduledTaskRunner.run(
                new ScheduledTaskEventConfiguration(SCHEDULER_NAME),
                searchResult,
                pollingEventEmitterScheduledTask
            );
        }
    }
}
