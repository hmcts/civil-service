package uk.gov.hmcts.reform.civil.ga.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.event.DispatchBusinessProcessEvent;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeClient;

import java.util.Map;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
@Component
public class GaEventEmitterService {

    public static final String TENANT_ID = "civil";
    public static final String CASE_ID = "caseId";
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CamundaRuntimeClient camundaRuntimeClient;

    public void emitBusinessProcessCamundaEvent(Long caseId, GeneralApplication application, boolean dispatchProcess) {
        var businessProcess = application.getBusinessProcess();
        var camundaEvent = businessProcess.getCamundaEvent();
        log.info(format("Emitting %s camunda event for case: %d", camundaEvent, caseId));
        boolean nullTenantAttempt = false;
        try {
            camundaRuntimeClient.correlateStartMessage(camundaEvent, TENANT_ID, Map.of(CASE_ID, caseId));

            if (dispatchProcess) {
                applicationEventPublisher.publishEvent(new DispatchBusinessProcessEvent(caseId, businessProcess));
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
                camundaRuntimeClient.correlateStartMessageWithoutTenant(camundaEvent, Map.of(CASE_ID, caseId));

                if (dispatchProcess) {
                    applicationEventPublisher.publishEvent(new DispatchBusinessProcessEvent(caseId, businessProcess));
                }
                log.info("Camunda event emitted successfully without tenant");
            } catch (Exception e) {
                log.error(format("Emitting %s camunda event failed for case: %d, message: %s",
                                 camundaEvent, caseId, e.getMessage()
                ));
            }
        }
    }

    public void emitBusinessProcessCamundaGAEvent(GeneralApplicationCaseData caseData, boolean dispatchProcess) {
        var caseId = caseData.getCcdCaseReference();
        var judgeBusinessProcess = caseData.getBusinessProcess();
        var camundaEvent = judgeBusinessProcess.getCamundaEvent();
        log.info(format("Emitting %s camunda event for case: %d", camundaEvent, caseId));
        boolean nullTenantAttempt = false;
        try {
            camundaRuntimeClient.correlateStartMessage(camundaEvent, TENANT_ID, Map.of(CASE_ID, caseId));

            if (dispatchProcess) {
                applicationEventPublisher.publishEvent(new DispatchBusinessProcessEvent(caseId, judgeBusinessProcess));
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
                camundaRuntimeClient.correlateStartMessageWithoutTenant(camundaEvent, Map.of(CASE_ID, caseId));

                if (dispatchProcess) {
                    applicationEventPublisher.publishEvent(new DispatchBusinessProcessEvent(
                        caseId,
                        judgeBusinessProcess
                    ));
                }
                log.info("Camunda event emitted successfully without tenant");
            } catch (Exception e) {
                log.error(format("Emitting %s camunda event failed for case: %d, message: %s",
                                 camundaEvent, caseId, e.getMessage()
                ));
            }
        }
    }
}
