package uk.gov.hmcts.reform.civil.scheduler.gaunlessorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_SCHEDULER_CHECK_UNLESS_ORDER_DEADLINE;

@Component
@RequiredArgsConstructor
@Slf4j
public class GAUnlessOrderScheduledTask implements ScheduledTask<GeneralApplicationCaseData, Long> {

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
        log.info("GAUnlessOrderScheduledTask::accept case {}", caseId);

        coreCaseDataService.triggerGaEvent(
            caseId,
            END_SCHEDULER_CHECK_UNLESS_ORDER_DEADLINE,
            getUpdatedCaseDataMapper(updateCaseData(caseData))
        );
    }

    public boolean hasExpiredUnlessOrderDeadline(GeneralApplicationCaseData caseData) {
        GAJudicialMakeAnOrder judicialDecisionMakeOrder = caseData.getJudicialDecisionMakeOrder();
        return judicialDecisionMakeOrder != null
            && judicialDecisionMakeOrder.getJudgeApproveEditOptionDateForUnlessOrder() != null
            && YesOrNo.NO.equals(judicialDecisionMakeOrder.getIsOrderProcessedByUnlessScheduler())
            && !LocalDate.now(LOCAL_ZONE).isBefore(judicialDecisionMakeOrder.getJudgeApproveEditOptionDateForUnlessOrder());
    }

    private GeneralApplicationCaseData updateCaseData(GeneralApplicationCaseData caseData) {
        GAJudicialMakeAnOrder judicialDecisionMakeOrder = caseData.getJudicialDecisionMakeOrder();
        return caseData.copy()
            .judicialDecisionMakeOrder(
                judicialDecisionMakeOrder.copy().setIsOrderProcessedByUnlessScheduler(YesOrNo.YES))
            .build();
    }

    private Map<String, Object> getUpdatedCaseDataMapper(GeneralApplicationCaseData caseData) {
        return caseData.toMap(mapper);
    }

    @Override
    public ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
        return defaultBackPressureConfiguration.getDefaultBackPressure();
    }
}
