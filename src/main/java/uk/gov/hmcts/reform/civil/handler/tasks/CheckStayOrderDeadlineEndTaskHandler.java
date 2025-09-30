package uk.gov.hmcts.reform.civil.handler.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ExternalTaskData;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApproveConsentOrder;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.search.CaseStateSearchService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static java.time.LocalDate.now;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STAY_THE_CLAIM;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnExpression("${judge.revisit.stayOrder.event.emitter.enabled:true}")
public class CheckStayOrderDeadlineEndTaskHandler extends BaseExternalTaskHandler {

    private final CaseStateSearchService caseSearchService;

    private final CoreCaseDataService coreCaseDataService;

    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper mapper;

    @Override
    public ExternalTaskData handleTask(ExternalTask externalTask) {
        List<CaseData> cases = getOrderMadeCasesThatAreEndingToday();
        log.info("Job '{}' found {} case(s)", externalTask.getTopicName(), cases.size());

        cases.forEach(this::fireEventForStateChange);
        return ExternalTaskData.builder().build();
    }

    private List<CaseData> getOrderMadeCasesThatAreEndingToday() {
        Set<CaseDetails> orderMadeCases = caseSearchService
                .getOrderMadeGeneralApplications(ORDER_MADE, STAY_THE_CLAIM);

        return orderMadeCases.stream()
                .map(cd -> {
                    CaseData converted = caseDetailsConverter.toCaseDataGA(cd);
                    if (converted == null) {
                        log.debug("DEBUG: Conversion returned null for caseId={}", cd.getId());
                    } else {
                        log.debug("DEBUG: Conversion produced CaseData with id={}, state={}, consentOrder={}, judicialOrder={}",
                                converted.getCcdCaseReference(),
                                converted.getCcdState(),
                                converted.getApproveConsentOrder(),
                                converted.getJudicialDecisionMakeOrder()
                        );
                    }
                    return converted;
                })
                .filter(isJudgeOrderStayDeadlineExpired.or(isConsentOrderStayDeadlineExpired))
                .peek(cd -> log.debug("DEBUG: Passed filter with caseId={}", cd.getCcdCaseReference()))
                .toList();

    }


    private void fireEventForStateChange(CaseData caseData) {
        Long caseId = caseData.getCcdCaseReference();
        log.info("Firing event END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE to check applications with ORDER_MADE "
                + "and with Application type Stay claim and its end date is today "
                + "for caseId: {}", caseId);

        coreCaseDataService.triggerGaEvent(caseId, END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE,
                getUpdatedCaseDataMapper(updateCaseData(caseData)));
        log.info("Checking state for caseId: {}", caseId);
    }

    private CaseData updateCaseData(CaseData caseData) {
        if (caseData.getApproveConsentOrder() != null) {
            GAApproveConsentOrder consentOrder = caseData.getApproveConsentOrder();
            caseData = caseData.toBuilder()
                    .approveConsentOrder(
                            consentOrder.toBuilder().isOrderProcessedByStayScheduler(YesOrNo.YES).build())
                    .build();

        } else {
            GAJudicialMakeAnOrder judicialDecisionMakeOrder = caseData.getJudicialDecisionMakeOrder();
            caseData = caseData.toBuilder()
                    .judicialDecisionMakeOrder(
                            judicialDecisionMakeOrder.toBuilder().isOrderProcessedByStayScheduler(YesOrNo.YES).build())
                    .build();
        }
        return caseData;
    }

    private final  Predicate<CaseData> isJudgeOrderStayDeadlineExpired = caseData ->
            caseData.getJudicialDecisionMakeOrder() != null
                    && caseData.getJudicialDecisionMakeOrder().getJudgeApproveEditOptionDate() != null
                    && (!now().isBefore(caseData.getJudicialDecisionMakeOrder().getJudgeApproveEditOptionDate()));

    private final Predicate<CaseData> isConsentOrderStayDeadlineExpired = caseData ->
            caseData.getApproveConsentOrder() != null
                    && (nonNull(caseData.getApproveConsentOrder().getConsentOrderDateToEnd()))
                    && (!now().isBefore(caseData.getApproveConsentOrder().getConsentOrderDateToEnd()));

    private Map<String, Object> getUpdatedCaseDataMapper(CaseData caseData) {
        return caseData.toMap(mapper);
    }
}
