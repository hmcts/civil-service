package uk.gov.hmcts.reform.civil.aspect;

import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowStateAllowedEventService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.aspect.NoOngoingBusinessProcessAspect.ERROR_MESSAGE;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ACKNOWLEDGE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.START_BUSINESS_PROCESS;

@ExtendWith(MockitoExtension.class)
class NoOngoingBusinessProcessAspectTest {

    @InjectMocks
    private NoOngoingBusinessProcessAspect aspect;

    @Mock
    private FlowStateAllowedEventService flowStateAllowedEventService;

    @Mock
    private SimpleStateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow stateFlow;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @SneakyThrows
    private void mockProceedingJoinPoint(AboutToStartOrSubmitCallbackResponse response) {
        when(proceedingJoinPoint.proceed()).thenReturn(response);
    }

    private CallbackParams createCallbackParams(String eventId, CaseData caseData) {
        return CallbackParamsBuilder.builder()
            .of(ABOUT_TO_START, caseData)
            .request(CallbackRequest.builder().eventId(eventId).build())
            .build();
    }

    @Nested
    class UserEventTests {

        @Test
        @SneakyThrows
        void shouldProceedWhenNoOngoingBusinessProcess() {
            AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder().build();
            mockProceedingJoinPoint(response);

            CallbackParams callbackParams = createCallbackParams(
                ACKNOWLEDGE_CLAIM.name(),
                CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            );

            Object result = aspect.checkOngoingBusinessProcess(proceedingJoinPoint, callbackParams);

            assertThat(result).isEqualTo(response);
            verify(proceedingJoinPoint).proceed();
        }

        @ParameterizedTest
        @SneakyThrows
        @NullSource
        @EnumSource(value = BusinessProcessStatus.class, names = "FINISHED", mode = EnumSource.Mode.INCLUDE)
        void shouldProceedWhenBusinessProcessStatusIsNullOrFinished(BusinessProcessStatus status) {
            AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder().build();
            mockProceedingJoinPoint(response);

            CallbackParams callbackParams = createCallbackParams(
                CREATE_CLAIM.name(),
                CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .businessProcess(BusinessProcess.builder().status(status).build())
                    .build()
            );

            Object result = aspect.checkOngoingBusinessProcess(proceedingJoinPoint, callbackParams);

            assertThat(result).isEqualTo(response);
            verify(proceedingJoinPoint).proceed();
        }

        @ParameterizedTest
        @SneakyThrows
        @EnumSource(value = BusinessProcessStatus.class, names = "FINISHED", mode = EnumSource.Mode.EXCLUDE)
        void shouldNotProceedWhenOngoingBusinessProcess(BusinessProcessStatus status) {
            AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_MESSAGE))
                .build();

            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
            when(stateFlow.getStateHistory()).thenReturn(List.of(State.from("state1")));

            CallbackParams callbackParams = createCallbackParams(
                CREATE_CLAIM.name(),
                CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .businessProcess(BusinessProcess.builder().status(status).build())
                    .build()
            );

            Object result = aspect.checkOngoingBusinessProcess(proceedingJoinPoint, callbackParams);

            assertThat(result).isEqualTo(response);
            verify(proceedingJoinPoint, never()).proceed();
        }

        @ParameterizedTest
        @SneakyThrows
        @EnumSource(value = BusinessProcessStatus.class, names = "FINISHED", mode = EnumSource.Mode.EXCLUDE)
        void shouldProceedWhenOngoingBusinessProcessOnSubmittedCallback(BusinessProcessStatus status) {
            AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder().build();
            mockProceedingJoinPoint(response);

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(SUBMITTED, CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .businessProcess(BusinessProcess.builder().status(status).build())
                    .build())
                .request(CallbackRequest.builder().eventId(CREATE_CLAIM.name()).build())
                .build();

            Object result = aspect.checkOngoingBusinessProcess(proceedingJoinPoint, callbackParams);

            assertThat(result).isEqualTo(response);
            verify(proceedingJoinPoint).proceed();
        }
    }

    @Nested
    class CamundaEventTests {

        @Test
        @SneakyThrows
        void shouldProceedWhenNoOngoingBusinessProcess() {
            AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder().build();
            mockProceedingJoinPoint(response);

            CallbackParams callbackParams = createCallbackParams(
                START_BUSINESS_PROCESS.name(),
                CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
            );

            Object result = aspect.checkOngoingBusinessProcess(proceedingJoinPoint, callbackParams);

            assertThat(result).isEqualTo(response);
            verify(proceedingJoinPoint).proceed();
        }

        @ParameterizedTest
        @SneakyThrows
        @NullSource
        @EnumSource(value = BusinessProcessStatus.class)
        void shouldProceedWhenBusinessProcessStatusIsNullOrFinished(BusinessProcessStatus status) {
            AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder().build();
            mockProceedingJoinPoint(response);

            CallbackParams callbackParams = createCallbackParams(
                START_BUSINESS_PROCESS.name(),
                CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .businessProcess(BusinessProcess.builder().status(status).build())
                    .build()
            );

            Object result = aspect.checkOngoingBusinessProcess(proceedingJoinPoint, callbackParams);

            assertThat(result).isEqualTo(response);
            verify(proceedingJoinPoint).proceed();
        }
    }
}
