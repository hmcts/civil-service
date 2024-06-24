package uk.gov.hmcts.reform.civil.service;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.community.rest.exception.RemoteProcessEngineException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.civil.event.DispatchBusinessProcessEvent;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventEmitterServiceTest {

    @InjectMocks
    private EventEmitterService eventEmitterService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private RemoteProcessEngineException mockedRemoteProcessEngineException;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private MessageCorrelationBuilder messageCorrelationBuilder;

    @BeforeEach
    void setup() {
        when(runtimeService.createMessageCorrelation(any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariable(any(), any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.tenantId(any())).thenReturn(messageCorrelationBuilder);
    }

    private CaseData buildCaseDataWithBusinessProcess(String event, long caseReference) {
        return CaseData.builder()
            .businessProcess(BusinessProcess.builder().camundaEvent(event).build())
            .ccdCaseReference(caseReference)
            .build();
    }

    private void verifyCommonInteractions(String event, long caseReference, boolean tenantId, int timesCalled) throws RemoteProcessEngineException {
        verify(runtimeService, times(timesCalled)).createMessageCorrelation(event);
        verify(messageCorrelationBuilder, times(timesCalled)).setVariable("caseId", caseReference);
        verify(messageCorrelationBuilder, times(timesCalled)).correlateStartMessage();
        if (tenantId) {
            verify(messageCorrelationBuilder, times(timesCalled)).tenantId("civil");
        } else {
            verify(messageCorrelationBuilder, times(timesCalled)).withoutTenantId();
        }
    }

    @Test
    void shouldSendMessageAndTriggerEvent_whenInvoked_withTenantId() throws RemoteProcessEngineException {
        CaseData caseData = buildCaseDataWithBusinessProcess("TEST_EVENT", 1L);

        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);

        verifyCommonInteractions("TEST_EVENT", 1L, true, 1);
        verify(applicationEventPublisher).publishEvent(new DispatchBusinessProcessEvent(1L, caseData.getBusinessProcess()));
    }

    @Test
    void shouldSendMessageAndTriggerEvent_whenInvoked_withoutTenantId() throws RemoteProcessEngineException {
        when(messageCorrelationBuilder.correlateStartMessage())
            .thenThrow(mockedRemoteProcessEngineException)
            .thenReturn(null);
        when(messageCorrelationBuilder.withoutTenantId()).thenReturn(messageCorrelationBuilder);

        CaseData caseData = buildCaseDataWithBusinessProcess("TEST_EVENT", 1L);

        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);

        verifyCommonInteractions("TEST_EVENT", 1L, false, 2);
        verify(applicationEventPublisher, times(2)).publishEvent(new DispatchBusinessProcessEvent(1L, caseData.getBusinessProcess()));
    }

    @Test
    void shouldSendMessageAndNotTriggerEvent_whenNotTrue_withTenantId() throws RemoteProcessEngineException {
        CaseData caseData = buildCaseDataWithBusinessProcess("TEST_EVENT", 1L);

        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, false);

        verifyCommonInteractions("TEST_EVENT", 1L, true, 1);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldSendMessageAndNotTriggerEvent_whenNotTrue_withoutTenantId() throws RemoteProcessEngineException {
        when(messageCorrelationBuilder.withoutTenantId()).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateStartMessage()).thenThrow(mockedRemoteProcessEngineException);

        CaseData caseData = buildCaseDataWithBusinessProcess("TEST_EVENT", 1L);

        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, false);

        verifyCommonInteractions("TEST_EVENT", 1L, false, 2);
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldHandleException_whenInvoked() throws RemoteProcessEngineException {
        when(messageCorrelationBuilder.correlateStartMessage()).thenThrow(mockedRemoteProcessEngineException);
        when(messageCorrelationBuilder.withoutTenantId()).thenReturn(messageCorrelationBuilder);

        CaseData caseData = buildCaseDataWithBusinessProcess("TEST_EVENT", 1L);

        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);

        verifyCommonInteractions("TEST_EVENT", 1L, false, 2);
    }
}
