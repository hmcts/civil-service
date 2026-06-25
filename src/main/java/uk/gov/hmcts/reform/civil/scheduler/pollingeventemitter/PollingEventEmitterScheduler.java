package uk.gov.hmcts.reform.civil.scheduler.pollingeventemitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskEventConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.CaseReadyBusinessProcessSearchService;
import uk.gov.hmcts.reform.civil.service.search.common.ElasticSearchResult;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.pollingEventEmitter", name = "enabled", havingValue = "true")
public class PollingEventEmitterScheduler implements CivilScheduler {

    private static final String SCHEDULER_NAME = "PollingEventEmitter";
    static final int FIFTY_MINUTES_IN_SECONDS = 3000;

    private final CaseReadyBusinessProcessSearchService searchService;
    private final ScheduledTaskRunner scheduledTaskRunner;
    private final PollingEventEmitterScheduledTask pollingEventEmitterScheduledTask;

    @Value("${polling.emitter.multiple.cases.delay.seconds:30}")
    private long multiCasesExecutionDelayInSeconds;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.pollingEventEmitter.cronExpression}")
    @SchedulerLock(name = "PollingEventEmitterScheduler_eventEmitter",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        log.info("Running {} scheduler", SCHEDULER_NAME);
        ElasticSearchResult searchResult = searchService.getElasticSearchResult();
        ElasticSearchResult limitedSearchResult = limitToScheduledWindow(searchResult);
        long totalCases = limitedSearchResult == null ? 0 : limitedSearchResult.totalResults();
        long delayMs = TimeUnit.SECONDS.toMillis(getDelaySeconds());

        scheduledTaskRunner.run(
            new ScheduledTaskEventConfiguration(SCHEDULER_NAME),
            limitedSearchResult,
            caseDetails -> pollingEventEmitterScheduledTask.accept(caseDetails, totalCases, delayMs)
        );
    }

    private ElasticSearchResult limitToScheduledWindow(ElasticSearchResult searchResult) {
        if (searchResult == null) {
            return null;
        }

        int limit = getProcessingLimit(searchResult.totalResults());
        if (limit == searchResult.totalResults()) {
            return searchResult;
        }

        log.info(
            "Limiting {} scheduler from {} case(s) to {} case(s) to fit within the 50 minute processing window",
            SCHEDULER_NAME,
            searchResult.totalResults(),
            limit
        );

        return new ElasticSearchResult(searchResult.caseDetailsStream().limit(limit), limit);
    }

    private int getProcessingLimit(int cases) {
        return Math.min(cases, (int) (FIFTY_MINUTES_IN_SECONDS / getDelaySeconds()));
    }

    private long getDelaySeconds() {
        return Math.max(1L, multiCasesExecutionDelayInSeconds);
    }
}
