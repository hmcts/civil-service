package uk.gov.hmcts.reform.civil.scheduler.takecaseoffline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.InterceptorChain;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.InterceptorContext;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.SchedulerInterceptor;
import uk.gov.hmcts.reform.civil.scheduler.common.interceptor.TaskAbortedException;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.flowstate.AllowedEventService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TAKE_CASE_OFFLINE;
import static uk.gov.hmcts.reform.civil.scheduler.common.interceptor.CaseInterceptorAttributes.CIVIL_CASE_DATA;

@Slf4j
@Component
@RequiredArgsConstructor
public class TakeCaseOfflineFlowStateInterceptor implements SchedulerInterceptor<CaseDetails> {

    private final AllowedEventService allowedEventService;
    private final IStateFlowEngine stateFlowEngine;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;

    @Override
    public void accept(InterceptorContext<CaseDetails> context, InterceptorChain<CaseDetails> chain) {
        CaseData caseData = getCaseData(context);
        if (allowedEventService.isAllowed(caseData, TAKE_CASE_OFFLINE)) {
            chain.next(context);
            return;
        }

        logNotAllowed(caseData);
        throw new TaskAbortedException("TAKE_CASE_OFFLINE not allowed in current flow state");
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
                TAKE_CASE_OFFLINE.name(),
                caseData.getCcdCaseReference(),
                FlowState.fromFullName(stateFlow.getState().getName()),
                stateHistoryBuilder
            );
        } catch (Exception e) {
            log.warn("Error during state flow evaluation.", e);
        }
    }
}
