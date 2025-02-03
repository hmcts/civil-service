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

    private static final String TEST_EVENT = "TEST_EVENT";
    private static final long CASE_ID = 1L;

    @BeforeEach
    void setup() {
        when(runtimeService.createMessageCorrelation(any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariable(any(), any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.tenantId(any())).thenReturn(messageCorrelationBuilder);
    }

    @Test
    void shouldSendMessageAndTriggerEvent_whenInvoked_withTenantId() {
        CaseData caseData = createCaseData(TEST_EVENT, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);
        verify(runtimeService).createMessageCorrelation(TEST_EVENT);
        verify(messageCorrelationBuilder).setVariable("caseId", CASE_ID);
        verify(messageCorrelationBuilder).tenantId("civil");
        verify(messageCorrelationBuilder).correlateStartMessage();
        verify(applicationEventPublisher).publishEvent(new DispatchBusinessProcessEvent(CASE_ID, caseData.getBusinessProcess()));
    }

    @Test
    void shouldSendMessageAndTriggerEvent_whenInvoked_withoutTenantId() {
        when(messageCorrelationBuilder.withoutTenantId()).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateStartMessage())
            .thenThrow(mockedRemoteProcessEngineException)
            .thenReturn(null);

        CaseData caseData = createCaseData(TEST_EVENT, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);

        verify(runtimeService, times(2)).createMessageCorrelation(TEST_EVENT);
        verify(messageCorrelationBuilder, times(2)).setVariable("caseId", CASE_ID);
        verify(messageCorrelationBuilder).withoutTenantId();
        verify(messageCorrelationBuilder, times(2)).correlateStartMessage();
        verify(applicationEventPublisher, times(2)).publishEvent(new DispatchBusinessProcessEvent(CASE_ID, caseData.getBusinessProcess()));
    }

    @Test
    void shouldSendMessageAndNotTriggerEvent_whenNotTrue_withTenantId() {
        CaseData caseData = createCaseData(TEST_EVENT, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, false);
        verify(runtimeService).createMessageCorrelation(TEST_EVENT);
        verify(messageCorrelationBuilder).correlateStartMessage();
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldSendMessageAndNotTriggerEvent_whenNotTrue_withoutTenantId() {
        when(messageCorrelationBuilder.withoutTenantId()).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateStartMessage()).thenThrow(mockedRemoteProcessEngineException);

        CaseData caseData = createCaseData(TEST_EVENT, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, false);

        verify(runtimeService, times(2)).createMessageCorrelation(TEST_EVENT);
        verify(messageCorrelationBuilder, times(2)).correlateStartMessage();
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldHandleException_whenInvoked() {
        when(messageCorrelationBuilder.withoutTenantId()).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateStartMessage()).thenThrow(mockedRemoteProcessEngineException);

        CaseData caseData = createCaseData(TEST_EVENT, CASE_ID);
        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);

        verify(runtimeService, times(2)).createMessageCorrelation(TEST_EVENT);
        verify(messageCorrelationBuilder, times(2)).correlateStartMessage();
    }

    private CaseData createCaseData(String event, long caseId) {
        BusinessProcess businessProcess = BusinessProcess.builder().camundaEvent(event).build();
        return CaseData.builder()
            .businessProcess(businessProcess)
            .ccdCaseReference(caseId)
            .build();
    }
}
