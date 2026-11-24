package uk.gov.hmcts.reform.civil.scheduler.garesponsedeadline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;

@Component
@RequiredArgsConstructor
@Slf4j
public class GAResponseDeadlineScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "GAResponseDeadline";

    private final GAResponseDeadlineSearchService searchService;
    private final ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    private final GAResponseDeadlineTask scheduledTask;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.ga-response-deadline.cronExpression}")
    @SchedulerLock(name = "GAResponseDeadlineScheduler_checkResponseDeadlines",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        scheduledTaskRunner.run(
            SCHEDULER_NAME,
            searchService::getElasticSearchResult,
            scheduledTask
        );
    }
}
