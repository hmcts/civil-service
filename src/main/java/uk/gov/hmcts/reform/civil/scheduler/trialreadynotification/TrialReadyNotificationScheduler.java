package uk.gov.hmcts.reform.civil.scheduler.trialreadynotification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.TrialReadyNotificationSearchService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.trial-ready-notification", name = "enabled", havingValue = "true")
public class TrialReadyNotificationScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "TrialReadyNotification";

    private final TrialReadyNotificationSearchService searchService;
    private final ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    private final TrialReadyNotificationScheduledTask trialReadyNotificationScheduledTask;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.trial-ready-notification.cronExpression}")
    @SchedulerLock(name = "TrialReadyNotificationScheduler_sendTrialReadyNotifications",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        scheduledTaskRunner.run(
            SCHEDULER_NAME,
            searchService::getElasticSearchResult,
            trialReadyNotificationScheduledTask
        );
    }
}
