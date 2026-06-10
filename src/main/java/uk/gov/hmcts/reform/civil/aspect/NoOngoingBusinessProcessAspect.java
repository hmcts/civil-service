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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.NoOngoingBPAllowedEventService;
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
    private final NoOngoingBPAllowedEventService noOngoingBPAllowedEventService;

    @Around("execution(* *(*)) && @annotation(NoOngoingBusinessProcess) && args(callbackParams))")
    public Object checkOngoingBusinessProcess(
        ProceedingJoinPoint joinPoint,
        CallbackParams callbackParams
    ) throws Throwable {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        boolean generalApplicationCaseType = callbackParams.isGeneralApplicationCaseType();

        if (callbackParams.getType() == SUBMITTED
            || caseEvent.isCamundaEvent()
            || hasNoOngoingBusinessProcess(callbackParams, generalApplicationCaseType)
            || noOngoingBPAllowedEventService.isAllowed(caseEvent)
        ) {
            return joinPoint.proceed();
        }

        logOngoingBusinessProcessBlock(caseEvent, callbackParams, generalApplicationCaseType);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of(ERROR_MESSAGE))
            .build();
    }

    private boolean hasNoOngoingBusinessProcess(CallbackParams callbackParams, boolean generalApplicationCaseType) {
        if (generalApplicationCaseType) {
            return callbackParams.getGeneralApplicationCaseData().hasNoOngoingBusinessProcess();
        }
        return callbackParams.getCaseData().hasNoOngoingBusinessProcess();
    }

    private void logOngoingBusinessProcessBlock(
        CaseEvent caseEvent,
        CallbackParams callbackParams,
        boolean generalApplicationCaseType
    ) {
        if (generalApplicationCaseType) {
            GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
            log.info(
                "{} is not allowed on the case {} due to ongoing business process",
                caseEvent.name(),
                caseData.getCcdCaseReference()
            );
            return;
        }

        CaseData caseData = callbackParams.getCaseData();
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        StringBuilder stateHistoryBuilder = new StringBuilder();
        stateFlow.getStateHistory().forEach(s -> {
            stateHistoryBuilder.append(s.getName());
            stateHistoryBuilder.append(", ");
        });

        BusinessProcess bp = caseData.getBusinessProcess();
        try {
            log.info(
                "{} is not allowed on the case {} due to ongoing business process "
                    + "[camundaEvent={}, processInstanceId={}, activityId={}, status={}, readyOn={}], "
                    + "current FlowState: {}, stateFlowHistory: {}",
                caseEvent.name(),
                caseData.getCcdCaseReference(),
                bp.getCamundaEvent(),
                bp.getProcessInstanceId(),
                bp.getActivityId(),
                bp.getStatus(),
                bp.getReadyOn(),
                FlowState.fromFullName(stateFlow.getState().getName()),
                stateHistoryBuilder
            );
        } catch (StateFlowException e) {
            log.warn("Error during state flow evaluation.", e);
        }
    }
}
