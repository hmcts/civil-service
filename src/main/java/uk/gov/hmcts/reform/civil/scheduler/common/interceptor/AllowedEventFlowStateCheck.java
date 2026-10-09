package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.flowstate.AllowedEventService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import static uk.gov.hmcts.reform.civil.scheduler.common.interceptor.CaseInterceptorAttributes.CIVIL_CASE_DATA;

/**
 * Aborts the task when the configured event is not allowed in the current flow state of the case.
 * Instances are created via {@link AllowedEventFlowStateCheckFactory}.
 */
@Slf4j
@RequiredArgsConstructor
public class AllowedEventFlowStateCheck implements SchedulerInterceptor<CaseDetails> {

    private final AllowedEventService allowedEventService;
    private final IStateFlowEngine stateFlowEngine;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseEvent event;

    @Override
    public void accept(InterceptorContext<CaseDetails> context, InterceptorChain<CaseDetails> chain) {
        CaseData caseData = getCaseData(context);
        if (allowedEventService.isAllowed(caseData, event)) {
            chain.next(context);
            return;
        }

        logNotAllowed(caseData);
        throw new TaskAbortedException(event.name() + " not allowed in current flow state");
    }

    @Override
    public int getOrder() {
        return 100;
    }

    private CaseData getCaseData(InterceptorContext<CaseDetails> context) {
        return context.getAttribute(CIVIL_CASE_DATA)
            .orElseGet(() -> caseDetailsConverter.toCaseData(coreCaseDataService.getCase(context.getItem().getId())));
    }

    private void logNotAllowed(CaseData caseData) {
        try {
            StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
            StringBuilder stateHistoryBuilder = new StringBuilder();
            stateFlow.getStateHistory().forEach(s -> {
                stateHistoryBuilder.append(s.getName());
                stateHistoryBuilder.append(", ");
            });

            log.info(
                "{} is not allowed on the case id {}, current FlowState: {}, stateFlowHistory: {}",
                event.name(),
                caseData.getCcdCaseReference(),
                FlowState.fromFullName(stateFlow.getState().getName()),
                stateHistoryBuilder
            );
        } catch (Exception e) {
            log.warn("Error during state flow evaluation.", e);
        }
    }
}
