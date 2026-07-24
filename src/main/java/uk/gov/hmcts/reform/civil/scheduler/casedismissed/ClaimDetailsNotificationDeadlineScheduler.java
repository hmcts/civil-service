package uk.gov.hmcts.reform.civil.scheduler.casedismissed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.ClaimDetailsNotificationDeadlineSearchService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.claim-details-notification-deadline", name = "enabled", havingValue = "true")
public class ClaimDetailsNotificationDeadlineScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "ClaimDetailsNotificationDeadline";

    private final ClaimDetailsNotificationDeadlineSearchService searchService;
    private final ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    private final CaseDismissedScheduledTask caseDismissedScheduledTask;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.claim-details-notification-deadline.cronExpression}")
    @SchedulerLock(name = "ClaimDetailsNotificationDeadlineScheduler_dismissCases",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        scheduledTaskRunner.run(
            SCHEDULER_NAME,
            searchService::getElasticSearchResult,
            caseDismissedScheduledTask
        );
    }
}
