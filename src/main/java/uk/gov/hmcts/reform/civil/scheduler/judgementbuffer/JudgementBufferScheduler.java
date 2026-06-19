package uk.gov.hmcts.reform.civil.scheduler.judgementbuffer;

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
import uk.gov.hmcts.reform.civil.service.search.JudgementBufferExpiredSearchService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.judgementBuffer", name = "enabled", havingValue = "true")
public class JudgementBufferScheduler implements CivilScheduler {

    private static final String SCHEDULER_NAME = "JudgementBuffer";

    private final JudgementBufferExpiredSearchService searchService;
    private final ScheduledTaskRunner scheduledTaskRunner;
    private final JudgementBufferScheduledTask judgementBufferScheduledTask;
    private final FeatureToggleService featureToggleService;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.judgementBuffer.cronExpression}")
    @SchedulerLock(name = "JudgementBufferScheduler_issueJudgement",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        if (featureToggleService.isJudgmentBufferEnabled()) {
            log.info("Running {} scheduler", SCHEDULER_NAME);
            scheduledTaskRunner.run(
                new ScheduledTaskEventConfiguration(SCHEDULER_NAME),
                searchService.getElasticSearchResult(),
                judgementBufferScheduledTask
            );
        }
    }
}
