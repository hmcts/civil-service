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
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.scheduler.common.interceptor.CaseInterceptorAttributes.CIVIL_CASE_DATA;

/**
 * Aborts the task when the configured event is not allowed in the current flow state of the case.
 *
 * <p>Operates exclusively on Civil claims ({@link CaseData}).
 * Instances are created via {@link AllowedEventFlowStateCheckFactory}.</p>
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

        logNotAllowed(caseData, context.getItem().getId());
        throw new TaskAbortedException(AbortCategory.FLOW_STATE_DISALLOWED, event.name() + " not allowed in current flow state");
    }

    @Override
    public int getOrder() {
        return 100;
    }

    private CaseData getCaseData(InterceptorContext<CaseDetails> context) {
        return context.getAttribute(CIVIL_CASE_DATA)
            .orElseGet(() -> {
                CaseData data = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(context.getItem().getId()));
                context.setAttribute(CIVIL_CASE_DATA, data);
                return data;
            });
    }

    private void logNotAllowed(CaseData caseData, Long caseId) {
        try {
            boolean isSpec = SPEC_CLAIM.equals(caseData.getCaseAccessCategory());
            StateFlow stateFlow = isSpec ? stateFlowEngine.evaluateSpec(caseData) : stateFlowEngine.evaluate(caseData);
            String stateFlowHistory = stateFlow.getStateHistory().stream()
                .map(State::getName)
                .collect(joining(", "));

            log.info(
                "{} is not allowed on the case id {}, current FlowState: {}, stateFlowHistory: {}",
                event.name(),
                caseId,
                FlowState.fromFullName(stateFlow.getState().getName()),
                stateFlowHistory
            );
        } catch (Exception e) {
            log.warn("Error during state flow evaluation.", e);
        }
    }
}
