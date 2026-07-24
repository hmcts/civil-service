package uk.gov.hmcts.reform.civil.scheduler.gaordermade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.scheduler.common.CivilScheduler;
import uk.gov.hmcts.reform.civil.scheduler.common.ListTaskResult;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskRunner;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STAY_THE_CLAIM;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scheduler.ga-order-made", name = "enabled", havingValue = "true")
public class GAOrderMadeScheduler implements CivilScheduler {

    public static final String SCHEDULER_NAME = "GAOrderMadeScheduler";
    private static final ZoneId LOCAL_ZONE = ZoneId.of("Europe/London");

    private final CaseStateSearchService searchService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ScheduledTaskRunner<GeneralApplicationCaseData, Long> scheduledTaskRunner;
    private final GAOrderMadeScheduledTask gaOrderMadeScheduledTask;

    @Override
    public String getName() {
        return SCHEDULER_NAME;
    }

    @Scheduled(cron = "${scheduler.ga-order-made.cronExpression}")
    @SchedulerLock(name = "GAOrderMadeScheduler_checkStayOrderDeadlines",
        lockAtMostFor = "${scheduler.lockAtMostFor}",
        lockAtLeastFor = "${scheduler.lockAtLeastFor}")
    @Override
    public void runScheduledTask() {
        scheduledTaskRunner.run(
            SCHEDULER_NAME,
            () -> {
                List<GeneralApplicationCaseData> applications = searchService
                    .getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM)
                    .stream()
                    .map(caseDetailsConverter::toGeneralApplicationCaseData)
                    .filter(this::hasExpiredStayDeadline)
                    .toList();
                return new ListTaskResult<>(applications, applications.size());
            },
            gaOrderMadeScheduledTask
        );
    }

    boolean hasExpiredStayDeadline(GeneralApplicationCaseData caseData) {
        return isJudgeOrderStayDeadlineExpired.or(isConsentOrderStayDeadlineExpired).test(caseData);
    }

    private final Predicate<GeneralApplicationCaseData> isJudgeOrderStayDeadlineExpired = caseData ->
        caseData.getJudicialDecisionMakeOrder() != null
            && caseData.getJudicialDecisionMakeOrder().getJudgeApproveEditOptionDate() != null
            && !LocalDate.now(LOCAL_ZONE).isBefore(caseData.getJudicialDecisionMakeOrder()
                                                       .getJudgeApproveEditOptionDate());

    private final Predicate<GeneralApplicationCaseData> isConsentOrderStayDeadlineExpired = caseData ->
        caseData.getApproveConsentOrder() != null
            && Objects.nonNull(caseData.getApproveConsentOrder().getConsentOrderDateToEnd())
            && !LocalDate.now(LOCAL_ZONE).isBefore(caseData.getApproveConsentOrder().getConsentOrderDateToEnd());
}
