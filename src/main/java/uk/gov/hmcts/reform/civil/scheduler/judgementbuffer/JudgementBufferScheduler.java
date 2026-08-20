package uk.gov.hmcts.reform.civil.scheduler.judgementbuffer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.search.judgementbuffer.JudgementBufferExpiredSearchService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.judgement-buffer", name = "enabled", havingValue = "true")
public class JudgementBufferScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "JudgementBuffer";

    private final JudgementBufferExpiredSearchService searchService;
    private final ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    private final JudgementBufferScheduledTask judgementBufferScheduledTask;
    private final FeatureToggleService featureToggleService;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.judgement-buffer.cronExpression}")
    @SchedulerLock(name = "JudgementBufferScheduler_issueJudgement",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        if (featureToggleService.isJudgmentBufferEnabled()) {
            scheduledTaskRunner.run(
                SCHEDULER_NAME,
                searchService::getElasticSearchResult,
                judgementBufferScheduledTask
            );
        }
    }
}
