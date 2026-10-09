package uk.gov.hmcts.reform.civil.scheduler.trialreadynotification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.AllowedEventFlowStateCheckFactory;
import uk.gov.hmcts.reform.civil.service.search.TrialReadyNotificationSearchService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIAL_READY_NOTIFICATION;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrialReadyNotificationScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "TrialReadyNotification";

    private final TrialReadyNotificationSearchService searchService;
    private final ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    private final TrialReadyNotificationScheduledTask trialReadyNotificationScheduledTask;
    private final AllowedEventFlowStateCheckFactory allowedEventFlowStateCheckFactory;

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
            ScheduledTaskConfiguration.<CaseDetails, Long>builder()
                .schedulerName(SCHEDULER_NAME)
                .searchResultSupplier(searchService::getElasticSearchResult)
                .scheduledTask(trialReadyNotificationScheduledTask)
                .interceptors(List.of(allowedEventFlowStateCheckFactory.forEvent(TRIAL_READY_NOTIFICATION)))
                .build()
        );
    }
}
