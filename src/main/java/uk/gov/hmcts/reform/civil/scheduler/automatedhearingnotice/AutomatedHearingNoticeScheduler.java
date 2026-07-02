package uk.gov.hmcts.reform.civil.scheduler.automatedhearingnotice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.UnnotifiedHearingsSearchService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.automated-hearing-notice", name = "enabled", havingValue = "true")
public class AutomatedHearingNoticeScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "AutomatedHearingNotice";

    private final UnnotifiedHearingsSearchService searchService;
    private final ScheduledTaskRunner<String, String> scheduledTaskRunner;
    private final AutomatedHearingNoticeScheduledTask scheduledTask;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.automated-hearing-notice.cronExpression}")
    @SchedulerLock(name = "AutomatedHearingNoticeScheduler_sendHearingNotices",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        scheduledTaskRunner.run(
            SCHEDULER_NAME,
            searchService::getUnnotifiedHearings,
            scheduledTask
        );
    }
}
