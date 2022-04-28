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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowStateAllowedEventService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import java.util.List;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class NoOngoingBusinessProcessAspect {

    public static final String ERROR_MESSAGE = "There is a technical issue causing a delay. "
        + "You do not need to do anything. Please come back later.";

    private final FlowStateAllowedEventService flowStateAllowedEventService;

    private final StateFlowEngine stateFlowEngine;

    @Around("execution(* *(*)) && @annotation(NoOngoingBusinessProcess) && args(callbackParams))")
    public Object checkOngoingBusinessProcess(
        ProceedingJoinPoint joinPoint,
        CallbackParams callbackParams
    ) throws Throwable {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        CaseData caseData = callbackParams.getCaseData();
        StringBuilder stateHistoryBuilder = new StringBuilder();
        FlowState flowState = flowStateAllowedEventService.getFlowState(caseData);
        stateFlowEngine.evaluate(caseData).getStateHistory().forEach(s -> {
            stateHistoryBuilder.append(s.getName());
            stateHistoryBuilder.append(", ");
        });
        if (callbackParams.getType() == SUBMITTED
            || caseEvent.isCamundaEvent()
            || caseData.hasNoOngoingBusinessProcess()
            || valueOf(CaseEvent.INITIATE_GENERAL_APPLICATION).equals(callbackParams.getRequest().getEventId())
        ) {
            return joinPoint.proceed();
        }
        log.info(format(
            "%s is not allowed on the case %s due to ongoing business process, current FlowState: %s, "
                + "stateFlowHistory: %s",
            caseEvent.name(),
            caseData.getCcdCaseReference(),
            flowState, stateHistoryBuilder.toString()
        ));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of(ERROR_MESSAGE))
            .build();
    }
}
