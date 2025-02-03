package uk.gov.hmcts.reform.civil.aspect;

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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.EventEmitterService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;

@ExtendWith(MockitoExtension.class)
class EventEmitterAspectTest {

    @InjectMocks
    private EventEmitterAspect aspect;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Mock
    private EventEmitterService eventEmitterService;

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
