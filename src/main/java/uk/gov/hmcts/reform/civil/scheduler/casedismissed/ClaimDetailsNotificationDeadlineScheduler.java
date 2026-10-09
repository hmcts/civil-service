package uk.gov.hmcts.reform.civil.scheduler.casedismissed;

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
import uk.gov.hmcts.reform.civil.service.search.ClaimDetailsNotificationDeadlineSearchService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISMISS_CLAIM;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClaimDetailsNotificationDeadlineScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "ClaimDetailsNotificationDeadline";

    private final ClaimDetailsNotificationDeadlineSearchService searchService;
    private final ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    private final CaseDismissedScheduledTask caseDismissedScheduledTask;
    private final AllowedEventFlowStateCheckFactory allowedEventFlowStateCheckFactory;

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
            ScheduledTaskConfiguration.<CaseDetails, Long>builder()
                .schedulerName(SCHEDULER_NAME)
                .searchResultSupplier(searchService::getElasticSearchResult)
                .scheduledTask(caseDismissedScheduledTask)
                .interceptors(List.of(allowedEventFlowStateCheckFactory.forEvent(DISMISS_CLAIM)))
                .build()
        );
    }
}
