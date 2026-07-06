package uk.gov.hmcts.reform.civil.scheduler.managestaywatask;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.ManageStayUpdateRequestedSearchService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.manage-stay-wa-task", name = "enabled", havingValue = "true")
public class ManageStayWATaskScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "ManageStayWATask";

    private final ManageStayUpdateRequestedSearchService searchService;
    private final ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    private final ManageStayWATaskScheduledTask manageStayWATaskScheduledTask;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.manage-stay-wa-task.cronExpression}")
    @SchedulerLock(name = "ManageStayWATaskScheduler_manageWATasks",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        scheduledTaskRunner.run(
            SCHEDULER_NAME,
            searchService::getElasticSearchResult,
            manageStayWATaskScheduledTask
        );
    }
}
