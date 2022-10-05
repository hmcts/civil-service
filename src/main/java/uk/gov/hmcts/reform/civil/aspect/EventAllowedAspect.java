package uk.gov.hmcts.reform.civil.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowStateAllowedEventService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class EventAllowedAspect {

    private static final String ERROR_MESSAGE = "This action cannot currently be performed because it has either "
        + "already been completed or another action must be completed first.";

    private final FlowStateAllowedEventService flowStateAllowedEventService;

    private final StateFlowEngine stateFlowEngine;

    @Pointcut("execution(* *(*)) && @annotation(EventAllowed)")
    public void eventAllowedPointCut() {
        //Pointcut no implementation required
    }

    @Around("eventAllowedPointCut() && args(callbackParams))")
    public Object checkEventAllowed(
        ProceedingJoinPoint joinPoint,
        CallbackParams callbackParams
    ) throws Throwable {
        if (callbackParams.getType() != ABOUT_TO_START) {
            return joinPoint.proceed();
        }
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CaseData caseData = callbackParams.getCaseData();
        StringBuilder stateHistoryBuilder = new StringBuilder();
        FlowState flowState = flowStateAllowedEventService.getFlowState(caseData);
        stateFlowEngine.evaluate(caseData).getStateHistory().forEach(s -> {
            stateHistoryBuilder.append(s.getName());
            stateHistoryBuilder.append(", ");
        });
        if (flowStateAllowedEventService.isAllowed(caseDetails, caseEvent)) {
            return joinPoint.proceed();
        } else {
            log.info(format(
                "%s is not allowed on the case id %s, current FlowState: %s, stateFlowHistory: %s",
                caseEvent.name(), caseDetails.getId(), flowState, stateHistoryBuilder.toString()
            ));
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_MESSAGE))
                .build();
        }
    }
}
