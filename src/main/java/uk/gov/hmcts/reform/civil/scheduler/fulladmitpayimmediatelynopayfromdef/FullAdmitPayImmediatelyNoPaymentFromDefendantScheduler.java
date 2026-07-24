package uk.gov.hmcts.reform.civil.scheduler.fulladmitpayimmediatelynopayfromdef;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;
import uk.gov.hmcts.reform.civil.service.search.FullAdmitPayImmediatelyNoPaymentFromDefendantSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.full-admit-pay-immediately-no-payment-from-def", name = "enabled", havingValue = "true")
public class FullAdmitPayImmediatelyNoPaymentFromDefendantScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "FullAdmitPayImmediatelyNoPaymentFromDefendant";
    private final FullAdmitPayImmediatelyNoPaymentFromDefendantSearchService searchService;
    private final ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    private final FullAdmitPayImmediatelyNoPaymentFromDefendantScheduledTask fullAdmitPayImmediatelyNoPaymentFromDefendantScheduledTask;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.full-admit-pay-immediately-no-payment-from-def.cronExpression}")
    @SchedulerLock(name = "FullAdmitPayImmediatelyNoPaymentFromDefScheduler_fullAdmitPay",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        scheduledTaskRunner.run(
            SCHEDULER_NAME,
            searchService::getElasticSearchResult,
            fullAdmitPayImmediatelyNoPaymentFromDefendantScheduledTask
        );
    }
}
