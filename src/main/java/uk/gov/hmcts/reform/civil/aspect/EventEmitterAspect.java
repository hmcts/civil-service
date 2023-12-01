package uk.gov.hmcts.reform.civil.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.EventEmitterService;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class EventEmitterAspect {

    private final EventEmitterService eventEmitterService;

    @Around("execution(* *(*)) && @annotation(EventEmitter) && args(callbackParams))")
    public Object emitBusinessProcessEvent(ProceedingJoinPoint joinPoint, CallbackParams callbackParams)
        throws Throwable {
        var data = callbackParams.getCaseData();
        if(data != null) {
            System.out.println("TEST_LOGGING - CASE EVENT: " + callbackParams.getRequest().getEventId());
            if(data.getApplicant1() != null) {
                System.out.println("TEST_LOGGING - APPLICANT1 ADDRESS: ");
                System.out.println(data.getApplicant1().getPrimaryAddress() != null ? "EXISTS" : "MISSING");
            }

            if(data.getRespondent1() != null) {
                System.out.println("TEST_LOGGING - RESPONDENT1 ADDRESS: ");
                System.out.println(data.getRespondent1().getPrimaryAddress() != null ? "EXISTS" : "MISSING");
            }
        }

        if (callbackParams.getType() == SUBMITTED) {
            CaseData caseData = callbackParams.getCaseData();
            var businessProcess = caseData.getBusinessProcess();
            var camundaEvent = businessProcess.getCamundaEvent();
            var caseId = caseData.getCcdCaseReference();
            if (businessProcess != null && businessProcess.getStatus() == READY) {
                log.info(format("Emitting %s camunda event for case through submitted callback: %d",
                                camundaEvent, caseId));
                eventEmitterService.emitBusinessProcessCamundaEvent(caseData, false);
            }
        }
        return joinPoint.proceed();
    }
}
