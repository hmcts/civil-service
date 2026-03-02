package uk.gov.hmcts.reform.civil.aspect;

import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaEventEmitterService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.EventEmitterService;

import java.util.List;

import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION_COSC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAKE_DECISION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_APPLICATION;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class EventEmitterAspectTest {

    @InjectMocks
    private EventEmitterAspect aspect;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Mock
    private EventEmitterService eventEmitterService;

    @Mock
    private GaEventEmitterService gaEventEmitterService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @ParameterizedTest
    @EnumSource(value = CallbackType.class, mode = EnumSource.Mode.EXCLUDE, names = {"SUBMITTED"})
    void shouldNotEmitBusinessProcessEvent_whenCallbackIsNotSubmitted(CallbackType callbackType) throws Throwable {
        CallbackParams callbackParams = buildCallbackParams(callbackType, null);

        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verifyNoInteractions(eventEmitterService);
        verify(proceedingJoinPoint).proceed();
    }

    @ParameterizedTest
    @EnumSource(value = BusinessProcessStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"READY"})
    void shouldNotEmitBusinessProcessEvent_whenBusinessProcessStatusIsNotReady(BusinessProcessStatus status) throws Throwable {
        CaseData caseData = buildCaseDataWithBusinessProcessStatus(status);
        CallbackParams callbackParams = buildCallbackParams(SUBMITTED, caseData);

        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verifyNoInteractions(eventEmitterService);
        verify(proceedingJoinPoint).proceed();
    }

    @Test
    void shouldEmitBusinessProcessEvent_whenCallbackIsSubmittedAndBusinessProcessStatusIsReady() throws Throwable {
        CaseData caseData = buildCaseDataWithBusinessProcessStatus(READY);
        CallbackParams callbackParams = buildCallbackParams(SUBMITTED, caseData);

        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verify(eventEmitterService).emitBusinessProcessCamundaEvent(caseData, false);
        verify(proceedingJoinPoint).proceed();
    }

    @SneakyThrows
    @ParameterizedTest
    @EnumSource(value = BusinessProcessStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"READY"})
    void shouldNotEmitBusinessProcessCamundaEvent_whenBPStatusIsNotReadyAndPIIdnull(BusinessProcessStatus status) {
        final GeneralApplication generalApplication = GeneralApplication.builder()
            .businessProcess(new BusinessProcess().setStatus(status))
            .build();
        final CallbackParams callbackParams = callbackParamsForSubmittedGeneralApplicationCase(
            1L,
            generalApplication,
            RESPOND_TO_APPLICATION
        );

        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verifyNoInteractions(eventEmitterService);
        verify(proceedingJoinPoint).proceed();
    }

    @SneakyThrows
    @ParameterizedTest
    @EnumSource(value = BusinessProcessStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"READY"})
    void shouldNotEmitBusinessProcessGACamundaEvent_whenBPStatusIsNotReadyAndPIIdnull(BusinessProcessStatus status) {
        final GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .businessProcess(new BusinessProcess().setStatus(status))
            .build();
        final CaseDetails caseDetails = CaseDetailsBuilder.builder()
            .id(1L)
            .data(caseData)
            .build();

        when(caseDetailsConverter.toGeneralApplicationCaseData(eq(caseDetails))).thenReturn(caseData);

        final CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(SUBMITTED, caseDetails)
            .request(CallbackRequest.builder()
                         .caseDetails(caseDetails)
                         .eventId(RESPOND_TO_APPLICATION.name())
                         .build())
            .isGeneralApplicationCase(true)
            .build();

        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verifyNoInteractions(eventEmitterService);
        verify(proceedingJoinPoint).proceed();
    }

    @SneakyThrows
    @Test
    void shouldNotEmitBusinessProcessCamundaEvent_whenGAIsNull() {
        final GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .build();
        final CaseDetails caseDetails = CaseDetailsBuilder.builder()
            .id(1L)
            .data(caseData)
            .build();

        when(caseDetailsConverter.toGeneralApplicationCaseData(eq(caseDetails))).thenReturn(caseData);

        final CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(SUBMITTED, caseDetails)
            .request(CallbackRequest.builder()
                         .caseDetails(caseDetails)
                         .eventId(RESPOND_TO_APPLICATION.name())
                         .build())
            .isGeneralApplicationCase(true)
            .build();

        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verifyNoInteractions(eventEmitterService);
        verify(proceedingJoinPoint).proceed();
    }

    @SneakyThrows
    @Test
    void shouldEmitBusinessProcessCamundaEvent_whenCallbackIsSubmittedABPStatusIsReadyAndPIIsNotNull() {
        final Long caseId = 1L;
        final GeneralApplication generalApplication = GeneralApplication.builder()
            .businessProcess(BusinessProcess.ready(RESPOND_TO_APPLICATION))
            .build();
        final CallbackParams callbackParams = callbackParamsForSubmittedGeneralApplicationCase(
            caseId,
            generalApplication,
            RESPOND_TO_APPLICATION
        );

        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verify(gaEventEmitterService).emitBusinessProcessCamundaEvent(caseId, generalApplication, false);
        verify(proceedingJoinPoint).proceed();
    }

    @SneakyThrows
    @Test
    void shouldEmitBusinessProcessGACamundaEvent_whenCallbackIsSubmittedABPStatusIsReadyAndPIIsNotNull() {
        final Long caseId = 1L;
        final GeneralApplication generalApplication = GeneralApplication.builder()
            .businessProcess(BusinessProcess.ready(MAKE_DECISION))
            .build();
        final CallbackParams callbackParams = callbackParamsForSubmittedGeneralApplicationCase(caseId, generalApplication, MAKE_DECISION);

        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verify(gaEventEmitterService).emitBusinessProcessCamundaEvent(caseId, generalApplication, false);
        verify(proceedingJoinPoint).proceed();
    }

    @SneakyThrows
    @Test
    void shouldEmitBusinessProcessCamundaEvent_whenCallbackIsSubmittedAndEventIsInitiateGeneralApplication() {
        final Long caseId = 1L;
        final GeneralApplication generalApplication = GeneralApplication.builder()
            .businessProcess(BusinessProcess.ready(INITIATE_GENERAL_APPLICATION))
            .build();
        final CallbackParams callbackParams = callbackParamsForSubmittedCivilCase(
            caseId,
            generalApplication,
            INITIATE_GENERAL_APPLICATION
        );

        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verify(gaEventEmitterService).emitBusinessProcessCamundaEvent(caseId, generalApplication, false);
        verify(proceedingJoinPoint).proceed();
    }

    @SneakyThrows
    @Test
    void shouldEmitBusinessProcessCamundaEvent_whenCallbackIsSubmittedAndEventIsInitiateGeneralApplicationCosc() {
        final Long caseId = 1L;
        final GeneralApplication generalApplication = GeneralApplication.builder()
            .businessProcess(BusinessProcess.ready(INITIATE_GENERAL_APPLICATION_COSC))
            .build();
        final CallbackParams callbackParams = callbackParamsForSubmittedCivilCase(
            caseId,
            generalApplication,
            INITIATE_GENERAL_APPLICATION_COSC
        );

        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verify(gaEventEmitterService).emitBusinessProcessCamundaEvent(caseId, generalApplication, false);
        verify(proceedingJoinPoint).proceed();
    }

    private CallbackParams callbackParamsForSubmittedGeneralApplicationCase(Long caseId, GeneralApplication generalApplication, CaseEvent event) {
        return callbackParamsForSubmittedWith(caseId, generalApplication, event, true);
    }

    private CallbackParams callbackParamsForSubmittedCivilCase(Long caseId, GeneralApplication generalApplication, CaseEvent event) {
        return callbackParamsForSubmittedWith(caseId, generalApplication, event, false);
    }

    private CallbackParams callbackParamsForSubmittedWith(Long caseId, GeneralApplication generalApplication, CaseEvent event, boolean isGaCase) {
        final List<Element<GeneralApplication>> newApplication = newArrayList();
        newApplication.add(element(generalApplication));
        final GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalApplications(newApplication)
            .ccdCaseReference(caseId)
            .build();
        final CaseDetails caseDetails = CaseDetailsBuilder.builder()
            .id(caseId)
            .data(caseData)
            .build();

        when(caseDetailsConverter.toGeneralApplicationCaseData(eq(caseDetails))).thenReturn(caseData);

        return CallbackParamsBuilder.builder()
            .of(SUBMITTED, caseDetails)
            .request(CallbackRequest.builder()
                         .caseDetails(caseDetails)
                         .eventId(event.name())
                         .build())
            .isGeneralApplicationCase(isGaCase)
            .build();
    }

    private CallbackParams buildCallbackParams(CallbackType callbackType, CaseData caseData) {
        return CallbackParamsBuilder.builder()
            .of(callbackType, caseData)
            .build();
    }

    private CaseData buildCaseDataWithBusinessProcessStatus(BusinessProcessStatus status) {
        return CaseData.builder()
            .businessProcess(new BusinessProcess().setStatus(status))
            .build();
    }
}
