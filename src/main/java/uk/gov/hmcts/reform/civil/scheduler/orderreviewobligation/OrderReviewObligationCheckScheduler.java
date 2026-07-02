package uk.gov.hmcts.reform.civil.scheduler.orderreviewobligation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.OrderReviewObligationSearchService;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.order-review-obligation-check", name = "enabled", havingValue = "true")
public class OrderReviewObligationCheckScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "OrderReviewObligationCheck";

    private final OrderReviewObligationSearchService searchService;
    private final ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    private final OrderReviewObligationCheckScheduledTask orderReviewObligationCheckScheduledTask;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.order-review-obligation-check.cronExpression}")
    @SchedulerLock(name = "OrderReviewObligationCheckScheduler_checkObligations",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        scheduledTaskRunner.run(
            SCHEDULER_NAME,
            searchService::getElasticSearchResult,
            orderReviewObligationCheckScheduledTask
        );
    }
}
