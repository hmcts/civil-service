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
import uk.gov.hmcts.reform.civil.service.search.takecaseoffline.TakeCaseOfflineSchedulerSearchService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TakeCaseOfflineScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "TakeCaseOffline";

    private final TakeCaseOfflineSchedulerSearchService searchService;
    private final ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    private final TakeCaseOfflineScheduledTask takeCaseOfflineScheduledTask;
    private final TakeCaseOfflineFlowStateInterceptor takeCaseOfflineFlowStateInterceptor;

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
                .interceptors(List.of(takeCaseOfflineFlowStateInterceptor))
                .build()
        );
    }
}
