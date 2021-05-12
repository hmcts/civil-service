package uk.gov.hmcts.reform.civil.service;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.civil.event.DispatchBusinessProcessEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {JacksonAutoConfiguration.class, CaseDetailsConverter.class})
class EventEmitterServiceTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @MockBean
    private RuntimeService runtimeService;

    @MockBean
    private MessageCorrelationBuilder messageCorrelationBuilder;

    private EventEmitterService eventEmitterService;

    @BeforeEach
    void setup() {
        eventEmitterService = new EventEmitterService(applicationEventPublisher, runtimeService);
        when(runtimeService.createMessageCorrelation(any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariable(any(), any())).thenReturn(messageCorrelationBuilder);
    }

    @Test
    void shouldSendMessageAndTriggerEvent_whenInvoked() {
        var businessProcess = BusinessProcess.builder().camundaEvent("TEST_EVENT").build();
        CaseData caseData = CaseData.builder()
            .businessProcess(businessProcess)
            .ccdCaseReference(1L)
            .build();

        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);

        verify(runtimeService).createMessageCorrelation("TEST_EVENT");
        verify(messageCorrelationBuilder).setVariable("caseId", 1L);
        verify(messageCorrelationBuilder).correlateStartMessage();
        verify(applicationEventPublisher).publishEvent(new DispatchBusinessProcessEvent(1L, businessProcess));
    }

    @Test
    void shouldSendMessageAndNotTriggerEvent_whenNotTrue() {
        when(messageCorrelationBuilder.correlateStartMessage()).thenThrow(new RuntimeException());
        var businessProcess = BusinessProcess.builder().camundaEvent("TEST_EVENT").build();
        CaseData caseData = CaseData.builder()
            .businessProcess(businessProcess)
            .ccdCaseReference(1L)
            .build();

        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, false);

        verify(runtimeService).createMessageCorrelation("TEST_EVENT");
        verify(messageCorrelationBuilder).setVariable("caseId", 1L);
        verify(messageCorrelationBuilder).correlateStartMessage();
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldHandleException_whenInvoked() {
        when(messageCorrelationBuilder.correlateStartMessage()).thenThrow(new RuntimeException());
        var businessProcess = BusinessProcess.builder().camundaEvent("TEST_EVENT").build();
        CaseData caseData = CaseData.builder()
            .businessProcess(businessProcess)
            .ccdCaseReference(1L)
            .build();

        eventEmitterService.emitBusinessProcessCamundaEvent(caseData, true);

        verify(runtimeService).createMessageCorrelation("TEST_EVENT");
        verify(messageCorrelationBuilder).setVariable("caseId", 1L);
        verify(messageCorrelationBuilder).correlateStartMessage();
        verifyNoInteractions(applicationEventPublisher);
    }
}
