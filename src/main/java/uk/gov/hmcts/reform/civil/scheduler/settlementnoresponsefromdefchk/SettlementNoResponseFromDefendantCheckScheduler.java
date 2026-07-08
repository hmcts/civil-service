package uk.gov.hmcts.reform.civil.scheduler.settlementnoresponsefromdefchk;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.SettlementNoResponseFromDefendantSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.settlement-no-response-from-defendant-check", name = "enabled", havingValue = "true")
public class SettlementNoResponseFromDefendantCheckScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "SettlementNoResponseFromDefendantCheck";
    private final ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    private final SettlementNoResponseFromDefendantSearchService searchService;
    private final SettlementNoResponseFromDefendantCheckScheduledTask scheduledTask;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.settlement-no-response-from-defendant-check.cronExpression}")
    @SchedulerLock(name = "SettlementNoResponseFromDefendantScheduler_check",
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
