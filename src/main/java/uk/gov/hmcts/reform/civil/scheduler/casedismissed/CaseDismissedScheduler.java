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
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.OnGoingBusinessProcessCheck;
import uk.gov.hmcts.reform.civil.service.search.CaseDismissedSearchService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaseDismissedScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "CaseDismissed";

    private final CaseDismissedSearchService searchService;
    private final ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    private final CaseDismissedScheduledTask caseDismissedScheduledTask;
    private final OnGoingBusinessProcessCheck<CaseDetails> onGoingBusinessProcessCheck;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.case-dismissed.cronExpression}")
    @SchedulerLock(name = "CaseDismissedScheduler_dismissCases",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        scheduledTaskRunner.run(ScheduledTaskConfiguration.<CaseDetails, Long>builder()
            .schedulerName(SCHEDULER_NAME)
            .searchResultSupplier(searchService::getElasticSearchResult)
            .scheduledTask(caseDismissedScheduledTask)
            .interceptors(List.of(onGoingBusinessProcessCheck))
            .build());
    }
}
