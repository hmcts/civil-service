package uk.gov.hmcts.reform.civil.scheduler.defendantresponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.DefendantResponseDeadlineCheckSearchService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.defendantResponse", name = "enabled", havingValue = "true")
public class DefendantResponseDeadlineScheduler {

    public static final String SCHEDULER_NAME = "DefendantResponseDeadline";

    private final DefendantResponseDeadlineCheckSearchService searchService;
    private final ScheduledTaskRunner scheduledTaskRunner;
    private final DefendantResponseDeadlineTask defendantResponseDeadlineTask;

    @Scheduled(cron = "${scheduler.defendantResponse.cronExpression}")
    @SchedulerLock(name = "DefendantResponseDeadlineScheduler_deadlineCheck",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    public void deadlineCheck() {
        log.info("Running {} scheduler", SCHEDULER_NAME);
        scheduledTaskRunner.run(
            new ScheduledTaskEventConfiguration(SCHEDULER_NAME),
            searchService::getCases,
            defendantResponseDeadlineTask
        );
    }
}
