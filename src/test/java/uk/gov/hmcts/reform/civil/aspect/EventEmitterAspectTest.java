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
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaEventEmitterService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.EventEmitterService;

import java.util.List;

import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MAKE_DECISION;
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
        GeneralApplication generalApplication = GeneralApplication.builder()
            .businessProcess(BusinessProcess.builder().status(status).build())
            .build();
        List<Element<GeneralApplication>> newApplication = newArrayList();
        newApplication.add(element(generalApplication));
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .generalApplications(newApplication)
            .build();
        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(SUBMITTED, caseData)
            .isGeneralApplicationCase(true)
            .build();

        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verifyNoInteractions(eventEmitterService);
        verify(proceedingJoinPoint).proceed();
    }

    @SneakyThrows
    @ParameterizedTest
    @EnumSource(value = BusinessProcessStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"READY"})
    void shouldNotEmitBusinessProcessGACamundaEvent_whenBPStatusIsNotReadyAndPIIdnull(BusinessProcessStatus status) {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .businessProcess(BusinessProcess.builder().status(status).build())
            .build();
        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(SUBMITTED, caseData)
            .isGeneralApplicationCase(true)
            .build();

        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verifyNoInteractions(eventEmitterService);
        verify(proceedingJoinPoint).proceed();
    }

    @SneakyThrows
    @Test
    void shouldNotEmitBusinessProcessCamundaEvent_whenGAIsNull() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .build();
        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(SUBMITTED, caseData)
            .isGeneralApplicationCase(true)
            .build();

        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verifyNoInteractions(eventEmitterService);
        verify(proceedingJoinPoint).proceed();
    }

    @SneakyThrows
    @Test
    void shouldEmitBusinessProcessCamundaEvent_whenCallbackIsSubmittedABPStatusIsReadyAndPIIsNotNull() {
        GeneralApplication generalApplication = GeneralApplication.builder()
            .businessProcess(BusinessProcess.ready(INITIATE_GENERAL_APPLICATION))
            .build();
        List<Element<GeneralApplication>> newApplication = newArrayList();
        newApplication.add(element(generalApplication));
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .generalApplications(newApplication)
            .ccdCaseReference(1L)
            .build();
        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(SUBMITTED, caseData)
            .isGeneralApplicationCase(true)
            .build();
        Long caseId = 1L;
        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verify(gaEventEmitterService).emitBusinessProcessCamundaEvent(caseId, generalApplication, false);
        verify(proceedingJoinPoint).proceed();
    }

    @SneakyThrows
    @Test
    void shouldEmitBusinessProcessGACamundaEvent_whenCallbackIsSubmittedABPStatusIsReadyAndPIIsNotNull() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .businessProcess(BusinessProcess.ready(MAKE_DECISION))
            .build();
        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(SUBMITTED, caseData)
            .isGeneralApplicationCase(true)
            .build();
        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verify(gaEventEmitterService).emitBusinessProcessCamundaGAEvent(caseData, false);
        verify(proceedingJoinPoint).proceed();
    }

    private CallbackParams buildCallbackParams(CallbackType callbackType, CaseData caseData) {
        return CallbackParamsBuilder.builder()
            .of(callbackType, caseData)
            .build();
    }

    private CaseData buildCaseDataWithBusinessProcessStatus(BusinessProcessStatus status) {
        return CaseData.builder()
            .businessProcess(BusinessProcess.builder().status(status).build())
            .build();
    }
}
