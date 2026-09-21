package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.event.DispatchBusinessProcessEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeClient;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
@Component
public class EventEmitterService {

    private static final String TENANT_ID = "civil";
    private static final String CASE_ID = "caseId";
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CamundaRuntimeClient camundaRuntimeClient;

    public void emitBusinessProcessCamundaEvent(CaseData caseData, boolean dispatchProcess) {
        var caseId = caseData.getCcdCaseReference();
        var businessProcess = caseData.getBusinessProcess();
        var camundaEvent = businessProcess.getCamundaEvent();
        log.info(format("Emitting %s camunda event for case: %d", camundaEvent, caseId));

        boolean nullTenantAttempt = false;
        try {
            if (dispatchProcess) {
                applicationEventPublisher.publishEvent(new DispatchBusinessProcessEvent(caseId, businessProcess));
            }
            if (camundaEvent.equals("queryManagementRaiseQuery")
                || camundaEvent.equals("queryManagementRespondQuery")) {
                CaseMessage latestQuery = caseData.getQueries().latest();
                String queryId = latestQuery != null ? latestQuery.getId() : null;
                Map<String, Object> queryVariables = new HashMap<>();
                queryVariables.put(CASE_ID, caseId);
                queryVariables.put("queryId", queryId);
                camundaRuntimeClient.correlateStartMessage(camundaEvent, TENANT_ID, queryVariables);
            } else {
                camundaRuntimeClient.correlateStartMessage(
                    camundaEvent, TENANT_ID, Map.of(CASE_ID, caseId));
            }
            log.info("Camunda event emitted successfully with tenant");
        } catch (FeignException.BadRequest ex) {
            // 400 is the engine reporting no start message definition for this tenant
            // (MismatchingMessageCorrelationException), the only case the without-tenant
            // retry is for. Anything else, including a RetryableException on a request
            // the engine may already have committed, must not correlate a second time.
            nullTenantAttempt = true;
        } catch (Exception e) {
            log.error(format("Emitting %s camunda event failed for case: %d, tenant: %s, message: %s",
                             camundaEvent, caseId, TENANT_ID, e.getMessage()
            ));
        }

        if (nullTenantAttempt) {
            try {
                if (dispatchProcess) {
                    applicationEventPublisher.publishEvent(new DispatchBusinessProcessEvent(caseId, businessProcess));
                }
                camundaRuntimeClient.correlateStartMessageWithoutTenant(
                    camundaEvent, Map.of(CASE_ID, caseId));
                log.info("Camunda event emitted successfully without tenant");
            } catch (Exception e) {
                log.error(format("Emitting %s camunda event failed for case: %d, message: %s",
                                 camundaEvent, caseId, e.getMessage()
                ));
            }
        }
    }
}
