package uk.gov.hmcts.reform.civil.scheduler.gaunlessorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.UNLESS_ORDER;

@Component
@RequiredArgsConstructor
@Slf4j
public class GAUnlessOrderScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "GAUnlessOrderScheduler";

    private final CaseStateSearchService searchService;
    private final ScheduledTaskRunner<CaseDetails, Long> scheduledTaskRunner;
    private final GAUnlessOrderScheduledTask gaUnlessOrderScheduledTask;
    private final GAUnlessOrderDeadlineFilter gaUnlessOrderDeadlineFilter;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.ga-unless-order.cronExpression}")
    @SchedulerLock(name = "GAUnlessOrderScheduler_checkUnlessOrderDeadlines",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        scheduledTaskRunner.run(
            SCHEDULER_NAME,
            () -> {
                List<CaseDetails> applications = searchService
                    .getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER)
                    .stream()
                    .filter(gaUnlessOrderDeadlineFilter::hasExpiredUnlessOrderDeadline)
                    .toList();
                return new ListTaskResult<>(applications);
            },
            gaUnlessOrderScheduledTask
        );
    }
}
