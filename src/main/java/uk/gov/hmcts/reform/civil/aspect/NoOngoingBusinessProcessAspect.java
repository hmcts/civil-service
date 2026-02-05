package uk.gov.hmcts.reform.civil.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.exception.StateFlowException;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class NoOngoingBusinessProcessAspect {

    public static final String ERROR_MESSAGE = "There is a technical issue causing a delay. "
        + "You do not need to do anything. Please come back later.";

    private final IStateFlowEngine stateFlowEngine;

    @Around("execution(* *(*)) && @annotation(NoOngoingBusinessProcess) && args(callbackParams))")
    public Object checkOngoingBusinessProcess(
        ProceedingJoinPoint joinPoint,
        CallbackParams callbackParams
    ) throws Throwable {

        if (callbackParams.isGeneralApplicationCaseType()) {
            return checkOngoingBusinessProcessGa(joinPoint, callbackParams);
        }

        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        CaseData caseData = callbackParams.getCaseData();

        if (callbackParams.getType() == SUBMITTED
            || caseEvent.isCamundaEvent()
            || caseData.hasNoOngoingBusinessProcess()
            || (caseEvent.equals(CaseEvent.migrateCase) || caseEvent.equals(CaseEvent.UPDATE_CASE_DATA))
        ) {
            return joinPoint.proceed();
        }

        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        StringBuilder stateHistoryBuilder = new StringBuilder();
        stateFlow.getStateHistory().forEach(s -> {
            stateHistoryBuilder.append(s.getName());
            stateHistoryBuilder.append(", ");
        });

        try {
            log.info(
                "{} is not allowed on the case {} due to ongoing business process, current FlowState: {}, stateFlowHistory: {}",
                caseEvent.name(),
                caseData.getCcdCaseReference(),
                FlowState.fromFullName(stateFlow.getState().getName()),
                stateHistoryBuilder
            );
        } catch (StateFlowException e) {
            log.warn("Error during state flow evaluation.", e);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of(ERROR_MESSAGE))
            .build();
    }

    private Object checkOngoingBusinessProcessGa(
        ProceedingJoinPoint joinPoint,
        CallbackParams callbackParams
    ) throws Throwable {

        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();

        if (callbackParams.getType() == SUBMITTED
            || caseEvent.isCamundaEvent()
            || caseEvent.equals(CaseEvent.APPLICATION_PROCEEDS_IN_HERITAGE)
            || caseData.hasNoOngoingBusinessProcess()) {
            return joinPoint.proceed();
        }

        log.info(
            "{} is not allowed on the case {} due to ongoing business process",
            caseEvent.name(),
            caseData.getCcdCaseReference()
        );

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of(ERROR_MESSAGE))
            .build();
    }
}
