package uk.gov.hmcts.reform.civil.scheduler.gaordermade;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE;

@Component
@RequiredArgsConstructor
@Slf4j
public class GAOrderMadeScheduledTask implements ScheduledTask<GeneralApplicationCaseData, Long> {

    private static final ZoneId LOCAL_ZONE = ZoneId.of("Europe/London");

    private final GaCoreCaseDataService coreCaseDataService;
    private final ObjectMapper mapper;
    private final DefaultBackPressureConfiguration defaultBackPressureConfiguration;

    @Override
    public Long getItemId(GeneralApplicationCaseData caseData) {
        return caseData.getCcdCaseReference();
    }

    @Override
    public void accept(GeneralApplicationCaseData caseData) {
        Long caseId = caseData.getCcdCaseReference();
        log.info("GAOrderMadeScheduledTask::accept case {}", caseId);

        coreCaseDataService.triggerGaEvent(
            caseId,
            END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE,
            getUpdatedCaseDataMapper(updateCaseData(caseData))
        );
    }

    public boolean hasExpiredStayDeadline(GeneralApplicationCaseData caseData) {
        return isJudgeOrderStayDeadlineExpired.or(isConsentOrderStayDeadlineExpired).test(caseData);
    }

    private GeneralApplicationCaseData updateCaseData(GeneralApplicationCaseData caseData) {
        if (caseData.getApproveConsentOrder() != null) {
            GAApproveConsentOrder consentOrder = caseData.getApproveConsentOrder();
            GAApproveConsentOrder updatedConsentOrder = new GAApproveConsentOrder()
                .setConsentOrderDescription(consentOrder.getConsentOrderDescription())
                .setConsentOrderDateToEnd(consentOrder.getConsentOrderDateToEnd())
                .setShowConsentOrderDate(consentOrder.getShowConsentOrderDate())
                .setIsOrderProcessedByStayScheduler(YesOrNo.YES);
            return caseData.copy()
                .approveConsentOrder(updatedConsentOrder)
                .build();
        }

        GAJudicialMakeAnOrder judicialDecisionMakeOrder = caseData.getJudicialDecisionMakeOrder();
        return caseData.copy()
            .judicialDecisionMakeOrder(
                judicialDecisionMakeOrder.copy().setIsOrderProcessedByStayScheduler(YesOrNo.YES))
            .build();
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

    private Map<String, Object> getUpdatedCaseDataMapper(GeneralApplicationCaseData caseData) {
        return caseData.toMap(mapper);
    }

    @Override
    public ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
        return defaultBackPressureConfiguration.getDefaultBackPressure();
    }
}
