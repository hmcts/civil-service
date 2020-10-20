package uk.gov.hmcts.reform.unspec.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.unspec.event.DispatchBusinessProcessEvent;
import uk.gov.hmcts.reform.unspec.model.CaseData;

@Slf4j
@RequiredArgsConstructor
@Component
public class EventEmitterService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final RuntimeService runtimeService;

    public void emitBusinessProcessCamundaEvent(CaseData caseData) {
        var caseId = caseData.getCcdCaseReference();
        var businessProcess = caseData.getBusinessProcess();
        var camundaEvent = businessProcess.getCamundaEvent();
        log.info(String.format("Emitting %s camunda event for case: %d", camundaEvent, caseId));
        try {
            runtimeService.createMessageCorrelation(camundaEvent)
                .setVariable("CCD_ID", caseId)
                .correlateStartMessage();
            applicationEventPublisher.publishEvent(new DispatchBusinessProcessEvent(caseId, businessProcess));
            log.info("Camunda event emitted successfully");
        } catch (Exception ex) {
            log.error(String.format("Emitting %s camunda event failed for case: %d, message: %s",
                                    camundaEvent, caseId, ex.getMessage()));
        }
    }
}
