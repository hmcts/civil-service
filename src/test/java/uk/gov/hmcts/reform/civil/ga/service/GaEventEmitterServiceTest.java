package uk.gov.hmcts.reform.civil.ga.service;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.community.rest.exception.RemoteProcessEngineException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.civil.event.DispatchBusinessProcessEvent;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;

import java.util.List;

import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class GaEventEmitterServiceTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private RemoteProcessEngineException mockedRemoteProcessEngineException;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private MessageCorrelationBuilder messageCorrelationBuilder;

    private GaEventEmitterService eventEmitterService;

    @BeforeEach
    void setup() {
        eventEmitterService = new GaEventEmitterService(applicationEventPublisher, runtimeService);
        when(runtimeService.createMessageCorrelation(any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariable(any(), any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.tenantId(any())).thenReturn(messageCorrelationBuilder);
    }

    @Test
    void shouldSendMessageAndTriggerEvent_whenInvoked_withTenantId() {
        var businessProcess = new BusinessProcess().setCamundaEvent("TEST_EVENT");
        GeneralApplication generalApplication = GeneralApplication.builder()
            .businessProcess(businessProcess)
            .build();
        List<Element<GeneralApplication>> newApplication = newArrayList();
        newApplication.add(element(generalApplication));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalApplications(newApplication)
            .ccdCaseReference(1L)
            .build();
        var caseId = caseData.getCcdCaseReference();

        eventEmitterService.emitBusinessProcessCamundaEvent(caseId, generalApplication, true);

        verify(runtimeService).createMessageCorrelation("TEST_EVENT");
        verify(messageCorrelationBuilder).tenantId("civil");
        verify(messageCorrelationBuilder).setVariable("caseId", 1L);
        verify(messageCorrelationBuilder).correlateStartMessage();
        verify(applicationEventPublisher).publishEvent(new DispatchBusinessProcessEvent(1L, businessProcess));
    }

    @Test
    void shouldSendMessageAndTriggerEvent_whenInvoked_withoutTenantId() {
        when(messageCorrelationBuilder.withoutTenantId()).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateStartMessage()).thenThrow(mockedRemoteProcessEngineException)
            .thenReturn(null);

        var businessProcess = new BusinessProcess().setCamundaEvent("TEST_EVENT");
        GeneralApplication generalApplication = GeneralApplication.builder()
            .businessProcess(businessProcess)
            .build();
        List<Element<GeneralApplication>> newApplication = newArrayList();
        newApplication.add(element(generalApplication));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalApplications(newApplication)
            .ccdCaseReference(1L)
            .build();
        var caseId = caseData.getCcdCaseReference();

        eventEmitterService.emitBusinessProcessCamundaEvent(caseId, generalApplication, true);

        verify(runtimeService, times(2)).createMessageCorrelation("TEST_EVENT");
        verify(messageCorrelationBuilder, times(2)).setVariable("caseId", 1L);
        verify(messageCorrelationBuilder).withoutTenantId();
        verify(messageCorrelationBuilder, times(2)).correlateStartMessage();
        verify(applicationEventPublisher).publishEvent(new DispatchBusinessProcessEvent(1L, businessProcess));
    }

    @Test
    void shouldSendMessageAndTriggerGAEvent_whenInvoked_withTenantId() {
        var businessProcess = new BusinessProcess().setCamundaEvent("TEST_EVENT");
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .businessProcess(businessProcess)
            .ccdCaseReference(1L)
            .build();

        eventEmitterService.emitBusinessProcessCamundaGAEvent(caseData, true);

        verify(runtimeService).createMessageCorrelation("TEST_EVENT");
        verify(messageCorrelationBuilder).tenantId("civil");
        verify(messageCorrelationBuilder).setVariable("caseId", 1L);
        verify(messageCorrelationBuilder).correlateStartMessage();
        verify(applicationEventPublisher).publishEvent(new DispatchBusinessProcessEvent(1L, businessProcess));
    }

    @Test
    void shouldSendMessageAndTriggerGAEvent_whenInvoked_withoutTenantId() {
        when(messageCorrelationBuilder.withoutTenantId()).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateStartMessage()).thenThrow(mockedRemoteProcessEngineException)
            .thenReturn(null);

        var businessProcess = new BusinessProcess().setCamundaEvent("TEST_EVENT");
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .businessProcess(businessProcess)
            .ccdCaseReference(1L)
            .build();

        eventEmitterService.emitBusinessProcessCamundaGAEvent(caseData, true);

        verify(runtimeService, times(2)).createMessageCorrelation("TEST_EVENT");
        verify(messageCorrelationBuilder, times(2)).setVariable("caseId", 1L);
        verify(messageCorrelationBuilder).withoutTenantId();
        verify(messageCorrelationBuilder, times(2)).correlateStartMessage();
        verify(applicationEventPublisher).publishEvent(new DispatchBusinessProcessEvent(1L, businessProcess));
    }

    @Test
    void shouldSendMessageAndNotTriggerEvent_whenNotTrue() {
        when(messageCorrelationBuilder.correlateStartMessage()).thenThrow(new RuntimeException());
        var businessProcess = new BusinessProcess().setCamundaEvent("TEST_EVENT");
        GeneralApplication generalApplication = GeneralApplication.builder()
            .businessProcess(businessProcess)
            .build();
        List<Element<GeneralApplication>> newApplication = newArrayList();
        newApplication.add(element(generalApplication));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalApplications(newApplication)
            .ccdCaseReference(1L)
            .build();
        var caseId = caseData.getCcdCaseReference();

        eventEmitterService.emitBusinessProcessCamundaEvent(caseId, generalApplication, false);

        verify(runtimeService).createMessageCorrelation("TEST_EVENT");
        verify(messageCorrelationBuilder).setVariable("caseId", 1L);
        verify(messageCorrelationBuilder).correlateStartMessage();
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldSendMessageAndNotTriggerGAEvent_whenNotTrue() {
        when(messageCorrelationBuilder.correlateStartMessage()).thenThrow(new RuntimeException());
        var businessProcess = new BusinessProcess().setCamundaEvent("TEST_EVENT");
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .businessProcess(businessProcess)
            .ccdCaseReference(1L)
            .build();

        eventEmitterService.emitBusinessProcessCamundaGAEvent(caseData, false);

        verify(runtimeService).createMessageCorrelation("TEST_EVENT");
        verify(messageCorrelationBuilder).setVariable("caseId", 1L);
        verify(messageCorrelationBuilder).correlateStartMessage();
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldHandleException_whenInvoked() {
        when(messageCorrelationBuilder.withoutTenantId()).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateStartMessage()).thenThrow(mockedRemoteProcessEngineException);
        var businessProcess = new BusinessProcess().setCamundaEvent("TEST_EVENT");
        GeneralApplication generalApplication = GeneralApplication.builder()
            .businessProcess(businessProcess)
            .build();
        List<Element<GeneralApplication>> newApplication = newArrayList();
        newApplication.add(element(generalApplication));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalApplications(newApplication)
            .ccdCaseReference(1L)
            .build();
        var caseId = caseData.getCcdCaseReference();

        eventEmitterService.emitBusinessProcessCamundaEvent(caseId, generalApplication, true);

        verify(runtimeService, times(2)).createMessageCorrelation("TEST_EVENT");
        verify(messageCorrelationBuilder, times(2)).correlateStartMessage();
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldHandleException_whenInvokedGA() {
        when(messageCorrelationBuilder.withoutTenantId()).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateStartMessage()).thenThrow(mockedRemoteProcessEngineException);
        var businessProcess = new BusinessProcess().setCamundaEvent("TEST_EVENT");

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .businessProcess(businessProcess)
            .ccdCaseReference(1L)
            .build();

        eventEmitterService.emitBusinessProcessCamundaGAEvent(caseData, true);

        verify(runtimeService, times(2)).createMessageCorrelation("TEST_EVENT");
        verify(messageCorrelationBuilder, times(2)).correlateStartMessage();
        verifyNoInteractions(applicationEventPublisher);
    }
}
