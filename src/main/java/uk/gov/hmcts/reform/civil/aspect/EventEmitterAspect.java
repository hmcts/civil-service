package uk.gov.hmcts.reform.civil.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaEventEmitterService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.service.EventEmitterService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class EventEmitterAspect {

    private final EventEmitterService eventEmitterService;
    private final GaEventEmitterService gaEventEmitterService;
    private final CaseDetailsConverter caseDetailsConverter;

    @Around("execution(* *(*)) && @annotation(EventEmitter) && args(callbackParams))")
    public Object emitBusinessProcessEvent(ProceedingJoinPoint joinPoint, CallbackParams callbackParams)
        throws Throwable {

        if (callbackParams.getType() == SUBMITTED) {
            if (callbackParams.isGeneralApplicationCase()) {
                GeneralApplicationCaseData caseData = (GeneralApplicationCaseData) callbackParams.getBaseCaseData();
                processGeneralApplicationBusinessProcessEvent(caseData);
            } else if (Objects.equals(callbackParams.getRequest().getEventId(), INITIATE_GENERAL_APPLICATION.name())) {
                GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(
                    callbackParams.getRequest().getCaseDetails());
                log.info(format(
                    "Emit business process event for INITIATE_GENERAL_APPLICATION submitted callback for caseId: %d",
                    caseData.getCcdCaseReference()
                ));
                processGeneralApplicationBusinessProcessEvent(caseData);
            } else {
                CaseData caseData = callbackParams.getCaseData();
                var caseId = caseData.getCcdCaseReference();
                var businessProcess = caseData.getBusinessProcess();
                if (businessProcess != null && businessProcess.getStatus() == READY) {
                    var camundaEvent = businessProcess.getCamundaEvent();
                    log.info(format(
                        "Emitting %s camunda event for case through submitted callback: %d",
                        camundaEvent, caseId
                    ));
                    eventEmitterService.emitBusinessProcessCamundaEvent(caseData, false);
                } else {
                    log.info(format("No business process with status of READY found for caseId: %d", caseId));
                }
            }
        }
        return joinPoint.proceed();
    }

    private void processGeneralApplicationBusinessProcessEvent(GeneralApplicationCaseData caseData) {
        var caseId = caseData.getCcdCaseReference();
        log.info("Callback type is SUBMITTED for caseId: {}", caseId);
        List<Element<GeneralApplication>> generalApplications = caseData.getGeneralApplications();

        if (generalApplications != null) {
            Optional<Element<GeneralApplication>> generalApplicationElementOptional = generalApplications.stream()
                .filter(app -> app.getValue() != null && app.getValue().getBusinessProcess() != null
                    && app.getValue().getBusinessProcess().getStatus() == BusinessProcessStatus.READY
                    && app.getValue().getBusinessProcess().getProcessInstanceId() == null).findFirst();
            if (generalApplicationElementOptional.isPresent()) {
                GeneralApplication generalApplicationElement = generalApplicationElementOptional.get().getValue();
                log.info("Emitting business process GA event for general application element from caseId: {}", caseId);
                gaEventEmitterService.emitBusinessProcessCamundaEvent(caseId, generalApplicationElement, false);
            }
        } else {
            if (caseData.getBusinessProcess() != null && caseData.getBusinessProcess().getStatus() == READY) {
                log.info("Emitting business process GA event for caseId: {}", caseId);
                gaEventEmitterService.emitBusinessProcessCamundaGAEvent(caseData, false);
            }
        }
    }
}
