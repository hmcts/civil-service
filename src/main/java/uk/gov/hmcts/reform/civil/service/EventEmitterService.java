package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
@Component
public class EventEmitterService {

    public static final String TENANT_ID = "civil";
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RuntimeService runtimeService;

    public void emitBusinessProcessCamundaEvent(CaseData caseData, boolean dispatchProcess) {
        var caseId = caseData.getCcdCaseReference();
        var businessProcess = caseData.getBusinessProcess();
        var camundaEvent = businessProcess.getCamundaEvent();
        log.info(format("Emitting %s camunda event for case: %d", camundaEvent, caseId));

        try {
            runtimeService.createMessageCorrelation(camundaEvent)
                .tenantId(TENANT_ID)
                .setVariable("caseId", caseId)
                .correlateStartMessage();
            log.info("Camunda event emitted successfully with tenant");
        } catch (Exception e) {
            log.error(format("Emitting %s camunda event failed for case: %d, tenant: %s, message: %s",
                             camundaEvent, caseId, TENANT_ID, e.getMessage()
            ));
        }
    }
}
