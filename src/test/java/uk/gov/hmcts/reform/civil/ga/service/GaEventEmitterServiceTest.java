package uk.gov.hmcts.reform.civil.ga.service;

import feign.FeignException;
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
import uk.gov.hmcts.reform.civil.service.camunda.CamundaRuntimeClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class GaEventEmitterServiceTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private FeignException mockedFeignException;

    @Mock
    private CamundaRuntimeClient camundaRuntimeClient;

    private GaEventEmitterService eventEmitterService;

    @BeforeEach
    void setup() {
        eventEmitterService = new GaEventEmitterService(applicationEventPublisher, camundaRuntimeClient);
    }

    @Test
    void shouldSendMessageAndTriggerEvent_whenInvoked_withTenantId() {
        var businessProcess = new BusinessProcess().setCamundaEvent("TEST_EVENT");
        GeneralApplication generalApplication = new GeneralApplication()
            .setBusinessProcess(businessProcess);
        List<Element<GeneralApplication>> newApplication = newArrayList();
        newApplication.add(element(generalApplication));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalApplications(newApplication)
            .ccdCaseReference(1L)
            .build();
        var caseId = caseData.getCcdCaseReference();

        eventEmitterService.emitBusinessProcessCamundaEvent(caseId, generalApplication, true);

        verify(camundaRuntimeClient).correlateStartMessage("TEST_EVENT", "civil", Map.of("caseId", 1L));
        verify(applicationEventPublisher).publishEvent(new DispatchBusinessProcessEvent(1L, businessProcess));
    }

    @Test
    void shouldSendMessageAndTriggerEvent_whenInvoked_withoutTenantId() {
        doThrow(mockedFeignException).when(camundaRuntimeClient)
            .correlateStartMessage(any(), any(), any());

        var businessProcess = new BusinessProcess().setCamundaEvent("TEST_EVENT");
        GeneralApplication generalApplication = new GeneralApplication()
            .setBusinessProcess(businessProcess);
        List<Element<GeneralApplication>> newApplication = newArrayList();
        newApplication.add(element(generalApplication));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalApplications(newApplication)
            .ccdCaseReference(1L)
            .build();
        var caseId = caseData.getCcdCaseReference();

        eventEmitterService.emitBusinessProcessCamundaEvent(caseId, generalApplication, true);

        verify(camundaRuntimeClient).correlateStartMessage("TEST_EVENT", "civil", Map.of("caseId", 1L));
        verify(camundaRuntimeClient).correlateStartMessageWithoutTenant("TEST_EVENT", Map.of("caseId", 1L));
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

        verify(camundaRuntimeClient).correlateStartMessage("TEST_EVENT", "civil", Map.of("caseId", 1L));
        verify(applicationEventPublisher).publishEvent(new DispatchBusinessProcessEvent(1L, businessProcess));
    }

    @Test
    void shouldSendMessageAndTriggerGAEvent_whenInvoked_withoutTenantId() {
        doThrow(mockedFeignException).when(camundaRuntimeClient)
            .correlateStartMessage(any(), any(), any());

        var businessProcess = new BusinessProcess().setCamundaEvent("TEST_EVENT");
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .businessProcess(businessProcess)
            .ccdCaseReference(1L)
            .build();

        eventEmitterService.emitBusinessProcessCamundaGAEvent(caseData, true);

        verify(camundaRuntimeClient).correlateStartMessage("TEST_EVENT", "civil", Map.of("caseId", 1L));
        verify(camundaRuntimeClient).correlateStartMessageWithoutTenant("TEST_EVENT", Map.of("caseId", 1L));
        verify(applicationEventPublisher).publishEvent(new DispatchBusinessProcessEvent(1L, businessProcess));
    }

    @Test
    void shouldSendMessageAndNotTriggerEvent_whenNotTrue() {
        doThrow(new RuntimeException()).when(camundaRuntimeClient)
            .correlateStartMessage(any(), any(), any());
        var businessProcess = new BusinessProcess().setCamundaEvent("TEST_EVENT");
        GeneralApplication generalApplication = new GeneralApplication()
            .setBusinessProcess(businessProcess);
        List<Element<GeneralApplication>> newApplication = newArrayList();
        newApplication.add(element(generalApplication));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalApplications(newApplication)
            .ccdCaseReference(1L)
            .build();
        var caseId = caseData.getCcdCaseReference();

        eventEmitterService.emitBusinessProcessCamundaEvent(caseId, generalApplication, false);

        verify(camundaRuntimeClient).correlateStartMessage("TEST_EVENT", "civil", Map.of("caseId", 1L));
        verify(camundaRuntimeClient, never()).correlateStartMessageWithoutTenant(any(), any());
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldSendMessageAndNotTriggerGAEvent_whenNotTrue() {
        doThrow(new RuntimeException()).when(camundaRuntimeClient)
            .correlateStartMessage(any(), any(), any());
        var businessProcess = new BusinessProcess().setCamundaEvent("TEST_EVENT");
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .businessProcess(businessProcess)
            .ccdCaseReference(1L)
            .build();

        eventEmitterService.emitBusinessProcessCamundaGAEvent(caseData, false);

        verify(camundaRuntimeClient).correlateStartMessage("TEST_EVENT", "civil", Map.of("caseId", 1L));
        verify(camundaRuntimeClient, never()).correlateStartMessageWithoutTenant(any(), any());
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldHandleException_whenInvoked() {
        doThrow(mockedFeignException).when(camundaRuntimeClient)
            .correlateStartMessage(any(), any(), any());
        doThrow(mockedFeignException).when(camundaRuntimeClient)
            .correlateStartMessageWithoutTenant(any(), any());
        var businessProcess = new BusinessProcess().setCamundaEvent("TEST_EVENT");
        GeneralApplication generalApplication = new GeneralApplication()
            .setBusinessProcess(businessProcess);
        List<Element<GeneralApplication>> newApplication = newArrayList();
        newApplication.add(element(generalApplication));
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalApplications(newApplication)
            .ccdCaseReference(1L)
            .build();
        var caseId = caseData.getCcdCaseReference();

        eventEmitterService.emitBusinessProcessCamundaEvent(caseId, generalApplication, true);

        verify(camundaRuntimeClient).correlateStartMessage("TEST_EVENT", "civil", Map.of("caseId", 1L));
        verify(camundaRuntimeClient).correlateStartMessageWithoutTenant("TEST_EVENT", Map.of("caseId", 1L));
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void shouldHandleException_whenInvokedGA() {
        doThrow(mockedFeignException).when(camundaRuntimeClient)
            .correlateStartMessage(any(), any(), any());
        doThrow(mockedFeignException).when(camundaRuntimeClient)
            .correlateStartMessageWithoutTenant(any(), any());
        var businessProcess = new BusinessProcess().setCamundaEvent("TEST_EVENT");

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .businessProcess(businessProcess)
            .ccdCaseReference(1L)
            .build();

        eventEmitterService.emitBusinessProcessCamundaGAEvent(caseData, true);

        verify(camundaRuntimeClient).correlateStartMessage("TEST_EVENT", "civil", Map.of("caseId", 1L));
        verify(camundaRuntimeClient).correlateStartMessageWithoutTenant("TEST_EVENT", Map.of("caseId", 1L));
        verifyNoInteractions(applicationEventPublisher);
    }
}
