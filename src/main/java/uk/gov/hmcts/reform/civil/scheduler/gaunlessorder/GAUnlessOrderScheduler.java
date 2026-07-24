package uk.gov.hmcts.reform.civil.scheduler.gaunlessorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.UNLESS_ORDER;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.ga-unless-order", name = "enabled", havingValue = "true")
public class GAUnlessOrderScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "GAUnlessOrderScheduler";
    private static final ZoneId LOCAL_ZONE = ZoneId.of("Europe/London");

    private final CaseStateSearchService searchService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ScheduledTaskRunner<GeneralApplicationCaseData, Long> scheduledTaskRunner;
    private final GAUnlessOrderScheduledTask gaUnlessOrderScheduledTask;

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
                List<GeneralApplicationCaseData> applications = searchService
                    .getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER)
                    .stream()
                    .map(caseDetailsConverter::toGeneralApplicationCaseData)
                    .filter(this::hasExpiredUnlessOrderDeadline)
                    .toList();
                return new ListTaskResult<>(applications, applications.size());
            },
            gaUnlessOrderScheduledTask
        );
    }

    boolean hasExpiredUnlessOrderDeadline(GeneralApplicationCaseData caseData) {
        GAJudicialMakeAnOrder judicialDecisionMakeOrder = caseData.getJudicialDecisionMakeOrder();
        return judicialDecisionMakeOrder != null
            && judicialDecisionMakeOrder.getJudgeApproveEditOptionDateForUnlessOrder() != null
            && YesOrNo.NO.equals(judicialDecisionMakeOrder.getIsOrderProcessedByUnlessScheduler())
            && !LocalDate.now(LOCAL_ZONE).isBefore(judicialDecisionMakeOrder.getJudgeApproveEditOptionDateForUnlessOrder());
    }
}
