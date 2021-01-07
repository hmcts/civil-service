package uk.gov.hmcts.reform.unspec.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.service.flowstate.FlowStateAllowedEventService;

import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class EventAllowedAspect {

    private static final String ERROR_MESSAGE = "This action cannot currently be performed because it has either "
        + "already been completed or another action must be completed first.";

    private final FlowStateAllowedEventService flowStateAllowedEventService;

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
        if (flowStateAllowedEventService.isAllowed(caseDetails, caseEvent)) {
            return joinPoint.proceed();
        } else {
            log.info(format(
                "%s is not allowed on the case id %s",
                caseEvent.name(), caseDetails.getId()
            ));
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_MESSAGE))
                .build();
        }
    }
}
