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
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.unspec.service.flowstate.FlowStateAllowedEventService;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.DEFENDANT_RESPONSE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    EventAllowedAspect.class,
    FlowStateAllowedEventService.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class})
class EventAllowedAspectTest {

    private static final String ERROR_MESSAGE = "This action cannot currently be performed because it has either "
        + "already been completed or another action must be completed first.";

    @Autowired
    EventAllowedAspect eventAllowedAspect;
    @MockBean
    ProceedingJoinPoint proceedingJoinPoint;

    @ParameterizedTest
    @EnumSource(value = CallbackType.class, mode = EnumSource.Mode.EXCLUDE, names = {"ABOUT_TO_START"})
    @SneakyThrows
    void shouldProceedToMethodInvocation_whenCallbackTypeIsNotAboutToStart(CallbackType callbackType) {
        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder().build();
        when(proceedingJoinPoint.proceed()).thenReturn(response);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(callbackType, CaseDetailsBuilder.builder().atStateCaseIssued().build())
            .build();
        Object result = eventAllowedAspect.checkEventAllowed(proceedingJoinPoint, callbackParams);

        assertThat(result).isEqualTo(response);
        verify(proceedingJoinPoint).proceed();
    }

    @Test
    @SneakyThrows
    void shouldNotProceedToMethodInvocation_whenEventIsNotAllowed() {
        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of(ERROR_MESSAGE))
            .build();
        when(proceedingJoinPoint.proceed()).thenReturn(response);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .type(ABOUT_TO_START)
            .request(CallbackRequest.builder()
                         .eventId(DEFENDANT_RESPONSE.name())
                         .caseDetails(CaseDetailsBuilder.builder().atStatePendingClaimIssued().build())
                         .build())
            .build();
        Object result = eventAllowedAspect.checkEventAllowed(proceedingJoinPoint, callbackParams);

        assertThat(result).isEqualTo(response);
        verify(proceedingJoinPoint, never()).proceed();
    }

    @Test
    @SneakyThrows
    void shouldProceedToMethodInvocation_whenEventIsAllowed() {
        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder().build();
        when(proceedingJoinPoint.proceed()).thenReturn(response);

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .type(ABOUT_TO_START)
            .request(CallbackRequest.builder()
                         .eventId(CLAIMANT_RESPONSE.name())
                         .caseDetails(CaseDetailsBuilder.builder().atStateRespondedToClaim().build())
                         .build())
            .build();
        Object result = eventAllowedAspect.checkEventAllowed(proceedingJoinPoint, callbackParams);

        assertThat(result).isEqualTo(response);
        verify(proceedingJoinPoint).proceed();
    }
}
