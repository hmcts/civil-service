package uk.gov.hmcts.reform.civil.ga.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.search.CaseStateSearchService;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_SCHEDULER_CHECK_UNLESS_ORDER_DEADLINE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.UNLESS_ORDER;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnExpression("${judge.revisit.unlessOrder.event.emitter.enabled:true}")
public class CheckUnlessOrderDeadlineEndTaskHandler extends BaseExternalTaskHandler {

    private final CaseStateSearchService caseSearchService;

    private final GaCoreCaseDataService coreCaseDataService;

    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        List<GeneralApplicationCaseData> cases = getUnlessOrderCasesThatAreEndingToday();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(this::fireEventForStateChange);
        return new ExternalTaskData();
    }

    private List<GeneralApplicationCaseData> getUnlessOrderCasesThatAreEndingToday() {
        Set<CaseDetails> unlessOrderCases = caseSearchService
            .getOrderMadeGeneralApplications(ORDER_MADE, UNLESS_ORDER);
        return unlessOrderCases.stream()
            .map(caseDetailsConverter::toGeneralApplicationCaseData)
            .filter(caseData -> caseData.getJudicialDecisionMakeOrder()
                .getJudgeApproveEditOptionDateForUnlessOrder() != null
                && caseData.getJudicialDecisionMakeOrder().getIsOrderProcessedByUnlessScheduler() != null
                && caseData.getJudicialDecisionMakeOrder().getIsOrderProcessedByUnlessScheduler().equals(YesOrNo.NO)
                && (!now().isBefore(caseData.getJudicialDecisionMakeOrder()
                                        .getJudgeApproveEditOptionDateForUnlessOrder()))
            ).toList();
    }

    private void fireEventForStateChange(GeneralApplicationCaseData caseData) {
        Long caseId = caseData.getCcdCaseReference();
        log.info("Firing event END_SCHEDULER_CHECK_UNLESS_ORDER_DEADLINE to check applications with ORDER_MADE "
                     + "and with Application type Unless Order and its end date is today "
                     + "for caseId: {}", caseId);

        coreCaseDataService.triggerGaEvent(caseId, END_SCHEDULER_CHECK_UNLESS_ORDER_DEADLINE,
                                           getUpdatedCaseDataMapper(updateCaseData(caseData)));
        log.info("Checking state for caseId: {}", caseId);
    }

    private GeneralApplicationCaseData updateCaseData(GeneralApplicationCaseData caseData) {
        GAJudicialMakeAnOrder judicialDecisionMakeOrder = caseData.getJudicialDecisionMakeOrder();
        caseData = caseData.copy()
            .judicialDecisionMakeOrder(
                judicialDecisionMakeOrder.copy().setIsOrderProcessedByUnlessScheduler(YesOrNo.YES))
            .build();
        return caseData;
    }

    private Map<String, Object> getUpdatedCaseDataMapper(GeneralApplicationCaseData caseData) {
        return caseData.toMap(mapper);
    }
}
