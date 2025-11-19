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
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowStateAllowedEventService;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.AllowedEventService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.exception.StateFlowException;

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

    private final AllowedEventService allowedEventService;
    private final GaFlowStateAllowedEventService gaFlowStateAllowedEventService;

    private final IStateFlowEngine stateFlowEngine;
    private final GaStateFlowEngine gaStateFlowEngine;

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

        if (allowedEventService.isAllowed(caseDetails, caseEvent)
            || gaFlowStateAllowedEventService.isAllowed(caseDetails, caseEvent)) {
            return joinPoint.proceed();
        } else {
            if (callbackParams.isGeneralApplicationCase()) {
                evaluateGeneralApplicationFlowState(callbackParams, caseEvent, caseDetails);
            } else {
                CaseData caseData = callbackParams.getCaseData();
                StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
                StringBuilder stateHistoryBuilder = new StringBuilder();
                stateFlow.getStateHistory().forEach(s -> {
                    stateHistoryBuilder.append(s.getName());
                    stateHistoryBuilder.append(", ");
                });

                try {
                    log.info(
                        "{} is not allowed on the case id {}, current FlowState: {}, stateFlowHistory: {}",
                        caseEvent.name(),
                        caseData.getCcdCaseReference(),
                        FlowState.fromFullName(stateFlow.getState().getName()),
                        stateHistoryBuilder
                    );
                } catch (StateFlowException e) {
                    log.warn("Error during state flow evaluation.", e);
                }
            }

            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_MESSAGE))
                .build();
        }
    }

    private void evaluateGeneralApplicationFlowState(CallbackParams callbackParams, CaseEvent caseEvent, CaseDetails caseDetails) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        StringBuilder stateHistoryBuilder = new StringBuilder();
        GaFlowState flowState = gaFlowStateAllowedEventService.getFlowState(caseData);
        gaStateFlowEngine.evaluate(caseData).getStateHistory().forEach(s -> {
            stateHistoryBuilder.append(s.getName());
            stateHistoryBuilder.append(", ");
        });
        log.info(format(
            "%s is not allowed on the case id %s, current FlowState: %s, stateFlowHistory: %s",
            caseEvent.name(), caseDetails.getId(), flowState, stateHistoryBuilder
        ));
    }
}
