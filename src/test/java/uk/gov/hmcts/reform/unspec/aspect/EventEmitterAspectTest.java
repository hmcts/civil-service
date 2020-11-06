package uk.gov.hmcts.reform.unspec.aspect;

import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.service.EventEmitterService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.READY;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    EventEmitterAspect.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class EventEmitterAspectTest {

    @Autowired
    EventEmitterAspect aspect;

    @Autowired
    CaseDetailsConverter caseDetailsConverter;

    @MockBean
    ProceedingJoinPoint proceedingJoinPoint;

    @MockBean
    EventEmitterService eventEmitterService;

    @SneakyThrows
    @ParameterizedTest
    @EnumSource(value = CallbackType.class, mode = EnumSource.Mode.EXCLUDE, names = {"SUBMITTED"})
    void shouldNotEmitBusinessProcessCamundaEvent_whenCallbackIsNotSubmitted(CallbackType callbackType) {
        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .type(callbackType)
            .build();

        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verifyNoInteractions(eventEmitterService);
        verify(proceedingJoinPoint).proceed();
    }

    @SneakyThrows
    @ParameterizedTest
    @EnumSource(value = BusinessProcessStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"READY"})
    void shouldNotEmitBusinessProcessCamundaEvent_whenBusinessProcessStatusIsNotReady(BusinessProcessStatus status) {
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().status(status).build())
            .build();
        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(SUBMITTED, caseData)
            .build();

        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verifyNoInteractions(eventEmitterService);
        verify(proceedingJoinPoint).proceed();
    }

    @SneakyThrows
    @Test
    void shouldEmitBusinessProcessCamundaEvent_whenCallbackIsSubmittedAndBusinessProcessStatusIsReady() {
        CaseData caseData = CaseData.builder()
            .businessProcess(BusinessProcess.builder().status(READY).build())
            .build();
        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(SUBMITTED, caseData)
            .build();

        aspect.emitBusinessProcessEvent(proceedingJoinPoint, callbackParams);

        verify(eventEmitterService).emitBusinessProcessCamundaEvent(caseData, false);
        verify(proceedingJoinPoint).proceed();
    }
}
