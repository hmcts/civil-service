package uk.gov.hmcts.reform.civil.scheduler.issuejudgement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.JudgmentRequestedSearchService;

@Component
@Slf4j
@RequiredArgsConstructor
public class JudgementBufferScheduler {

    public static final String SCHEDULER_NAME = "JudgementBuffer";

    private final JudgmentRequestedSearchService searchService;
    private final ScheduledTaskRunner scheduledTaskRunner;
    private final JudgementBufferScheduledTask judgementBufferScheduledTask;

    @Value("${scheduler.judgement-buffer.enabled:true}")
    private boolean isSchedulerEnabled;

    @Scheduled(cron = "${scheduler.judgement-buffer.cronExpression}")
    @SchedulerLock(name = "JudgementBufferScheduler_issueJudgement",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    public void issueJudgement() {
        if (isSchedulerEnabled) {
            scheduledTaskRunner.run(
                new ScheduledTaskEventConfiguration(SCHEDULER_NAME),
                searchService::getCases,
                judgementBufferScheduledTask
            );
        }
    }
}
