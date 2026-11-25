package uk.gov.hmcts.reform.civil.scheduler.takecaseoffline;

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
import uk.gov.hmcts.reform.civil.service.search.takecaseoffline.TakeCaseOfflineSchedulerSearchService;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TAKE_CASE_OFFLINE;

@Component
@RequiredArgsConstructor
@Slf4j
public class TakeCaseOfflineScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "TakeCaseOffline";

    private final TakeCaseOfflineSchedulerSearchService searchService;
    private final ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    private final TakeCaseOfflineScheduledTask takeCaseOfflineScheduledTask;
    private final AllowedEventFlowStateCheckFactory allowedEventFlowStateCheckFactory;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.take-case-offline.cronExpression}")
    @SchedulerLock(name = "TakeCaseOfflineScheduler_takeCasesOffline",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        scheduledTaskRunner.run(
            ScheduledTaskConfiguration.<CaseDetails, Long>builder()
                .schedulerName(SCHEDULER_NAME)
                .searchResultSupplier(searchService::getElasticSearchResult)
                .scheduledTask(takeCaseOfflineScheduledTask)
                .interceptors(List.of(allowedEventFlowStateCheckFactory.forEvent(TAKE_CASE_OFFLINE)))
                .build()
        );
    }
}
