package uk.gov.hmcts.reform.civil.scheduler.requestforreconsideration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.RequestForReconsiderationNotificationDeadlineSearchService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.request-for-reconsideration-notification", name = "enabled", havingValue = "true")
public class RequestForReconsiderationNotificationScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CHECK";

    private final RequestForReconsiderationNotificationDeadlineSearchService searchService;
    private final ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    private final RequestForReconsiderationNotificationScheduledTask requestForReconsiderationNotificationScheduledTask;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.request-for-reconsideration-notification.cronExpression}")
    @SchedulerLock(name = "RequestForReconsiderationNotificationScheduler_checkDeadlines",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        scheduledTaskRunner.run(
            SCHEDULER_NAME,
            searchService::getElasticSearchResult,
            requestForReconsiderationNotificationScheduledTask
        );
    }
}
