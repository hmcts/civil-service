package uk.gov.hmcts.reform.civil.scheduler.hearingfee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.HearingFeeDueSearchService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.hearing-fee", name = "enabled", havingValue = "true")
public class HearingFeeScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "HearingFee";

    private final HearingFeeDueSearchService searchService;
    private final ScheduledTaskRunner<CaseDetails, Long>  scheduledTaskRunner;
    private final HearingFeeSchedulerTask hearingFeeSchedulerTask;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.hearing-fee.cronExpression}")
    @SchedulerLock(name = "HearingFeeScheduler_check",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        scheduledTaskRunner.run(
            SCHEDULER_NAME,
            searchService::getElasticSearchResult,
            hearingFeeSchedulerTask
        );
    }
}
