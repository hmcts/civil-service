package uk.gov.hmcts.reform.civil.aspect;

import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.NoOngoingBPAllowedEventService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.aspect.NoOngoingBusinessProcessAspect.ERROR_MESSAGE;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ACKNOWLEDGE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.START_BUSINESS_PROCESS;

@ExtendWith(MockitoExtension.class)
class NoOngoingBusinessProcessAspectTest {

    @InjectMocks
    private NoOngoingBusinessProcessAspect aspect;

    @Mock
    private SimpleStateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow stateFlow;

    @Mock
    private NoOngoingBPAllowedEventService noOngoingBPAllowedEventService;

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
                    .businessProcess(new BusinessProcess().setStatus(status))
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

            State mockState = mock(State.class);
            when(mockState.getName()).thenReturn(FlowState.Main.DRAFT.fullName());
            when(stateFlow.getState()).thenReturn(mockState);
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
            when(stateFlow.getStateHistory()).thenReturn(List.of(mockState));

            CallbackParams callbackParams = createCallbackParams(
                CREATE_CLAIM.name(),
                CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .businessProcess(new BusinessProcess().setStatus(status))
                    .build()
            );

            Object result = aspect.checkOngoingBusinessProcess(proceedingJoinPoint, callbackParams);

            assertThat(result).isEqualTo(response);
            verify(proceedingJoinPoint, never()).proceed();
            verify(stateFlow).getStateHistory();
        }

        @ParameterizedTest
        @SneakyThrows
        @MethodSource("allowedCivilEvents")
        void shouldProceedWhenOngoingBusinessProcessForAllowedEvent(BusinessProcessStatus status, CaseEvent event) {
            AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder().build();
            mockProceedingJoinPoint(response);
            when(noOngoingBPAllowedEventService.isAllowed(event)).thenReturn(true);

            CallbackParams callbackParams = createCallbackParams(
                event.name(),
                CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .businessProcess(new BusinessProcess().setStatus(status))
                    .build()
            );

            Object result = aspect.checkOngoingBusinessProcess(proceedingJoinPoint, callbackParams);

            assertThat(result).isEqualTo(response);
            verify(proceedingJoinPoint).proceed();
        }

        private static Stream<Arguments> allowedCivilEvents() {
            return Stream.of(
                CaseEvent.ADD_OR_AMEND_CLAIM_DOCUMENTS,
                CaseEvent.CHANGE_SOLICITOR_EMAIL,
                CaseEvent.CREATE_CASE_FLAGS,
                CaseEvent.EVIDENCE_UPLOAD_JUDGE,
                CaseEvent.MANAGE_CASE_FLAGS,
                CaseEvent.MANAGE_DOCUMENTS,
                CaseEvent.ORDER_REVIEW_OBLIGATION_CHECK,
                CaseEvent.UPDATE_CASE_DATA,
                CaseEvent.REMOVE_DOCUMENT,
                CaseEvent.SERVICE_REQUEST_RECEIVED,
                CaseEvent.MODIFY_STATE_AFTER_ADDITIONAL_FEE_PAID,
                CaseEvent.APPLICATION_PROCEEDS_IN_HERITAGE,
                CaseEvent.migrateCase
            ).filter(event -> !event.isCamundaEvent()).flatMap(event -> Stream.of(
                BusinessProcessStatus.READY,
                BusinessProcessStatus.DISPATCHED,
                BusinessProcessStatus.STARTED
            ).map(status -> Arguments.of(status, event)));
        }

        @ParameterizedTest
        @SneakyThrows
        @EnumSource(value = BusinessProcessStatus.class, names = "FINISHED", mode = EnumSource.Mode.EXCLUDE)
        void shouldProceedWhenOngoingBusinessProcessOnSubmittedCallback(BusinessProcessStatus status) {
            AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder().build();
            mockProceedingJoinPoint(response);

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(SUBMITTED, CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .businessProcess(new BusinessProcess().setStatus(status))
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
                    .businessProcess(new BusinessProcess().setStatus(status))
                    .build()
            );

            Object result = aspect.checkOngoingBusinessProcess(proceedingJoinPoint, callbackParams);

            assertThat(result).isEqualTo(response);
            verify(proceedingJoinPoint).proceed();
        }
    }

    @Nested
    class GeneralApplicationUserEventTests {

        @Test
        @SneakyThrows
        void shouldProceedToMethodInvocation_whenNoOngoingBusinessProcess() {
            AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder().build();
            when(proceedingJoinPoint.proceed()).thenReturn(response);

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_START, GeneralApplicationCaseDataBuilder.builder().build())
                .isGeneralApplicationCase(true)
                .request(CallbackRequest.builder().eventId(INITIATE_GENERAL_APPLICATION.name()).build())
                .build();

            Object result = aspect.checkOngoingBusinessProcess(proceedingJoinPoint, callbackParams);

            assertThat(result).isEqualTo(response);
            verify(proceedingJoinPoint).proceed();
        }

        @SneakyThrows
        @ParameterizedTest
        @NullSource
        @EnumSource(value = BusinessProcessStatus.class, names = "FINISHED", mode = EnumSource.Mode.INCLUDE)
        void shouldProceedToMethodInvocation_whenBusinessProcessStatusIsNullOrFinished(BusinessProcessStatus status) {
            AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder().build();
            when(proceedingJoinPoint.proceed()).thenReturn(response);

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_START, GeneralApplicationCaseDataBuilder.builder()
                    .businessProcess(new BusinessProcess().setStatus(status))
                    .build())
                .isGeneralApplicationCase(true)
                .request(CallbackRequest.builder().eventId(INITIATE_GENERAL_APPLICATION.name()).build())
                .build();
            Object result = aspect.checkOngoingBusinessProcess(proceedingJoinPoint, callbackParams);

            assertThat(result).isEqualTo(response);
            verify(proceedingJoinPoint).proceed();
        }

        @SneakyThrows
        @ParameterizedTest
        @EnumSource(value = BusinessProcessStatus.class, names = "FINISHED", mode = EnumSource.Mode.EXCLUDE)
        void shouldNotProceedToMethodInvocation_whenOngoingBusinessProcess(BusinessProcessStatus status) {
            AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_MESSAGE))
                .build();

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_START, GeneralApplicationCaseDataBuilder.builder()
                    .businessProcess(new BusinessProcess().setStatus(status))
                    .build())
                .isGeneralApplicationCase(true)
                .request(CallbackRequest.builder().eventId(INITIATE_GENERAL_APPLICATION.name()).build())
                .build();

            Object result = aspect.checkOngoingBusinessProcess(proceedingJoinPoint, callbackParams);

            assertThat(result).isEqualTo(response);
            verify(proceedingJoinPoint, never()).proceed();
        }

        @SneakyThrows
        @ParameterizedTest
        @MethodSource("allowedGaEvents")
        void shouldProceedToMethodInvocation_whenOngoingBusinessProcessForAllowedGaEvent(
            BusinessProcessStatus status,
            CaseEvent event
        ) {
            AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder().build();
            when(proceedingJoinPoint.proceed()).thenReturn(response);
            when(noOngoingBPAllowedEventService.isAllowed(event)).thenReturn(true);

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_START, GeneralApplicationCaseDataBuilder.builder()
                    .businessProcess(new BusinessProcess().setStatus(status))
                    .build())
                .isGeneralApplicationCase(true)
                .request(CallbackRequest.builder().eventId(event.name()).build())
                .build();

            Object result = aspect.checkOngoingBusinessProcess(proceedingJoinPoint, callbackParams);

            assertThat(result).isEqualTo(response);
            verify(proceedingJoinPoint).proceed();
        }

        private static Stream<Arguments> allowedGaEvents() {
            return Stream.of(
                Arguments.of(BusinessProcessStatus.STARTED, INITIATE_GENERAL_APPLICATION_AFTER_PAYMENT),
                Arguments.of(BusinessProcessStatus.STARTED, CaseEvent.APPLICATION_PROCEEDS_IN_HERITAGE)
            );
        }

        @SneakyThrows
        @ParameterizedTest
        @EnumSource(value = BusinessProcessStatus.class, names = "FINISHED", mode = EnumSource.Mode.EXCLUDE)
        void shouldProceedToMethodInvocation_whenOngoingBusinessProcessOnSubmittedCallback(BusinessProcessStatus status
        ) {
            AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder().build();
            when(proceedingJoinPoint.proceed()).thenReturn(response);

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(SUBMITTED, GeneralApplicationCaseDataBuilder.builder()
                    .businessProcess(new BusinessProcess().setStatus(status))
                    .build())
                .isGeneralApplicationCase(true)
                .request(CallbackRequest.builder().eventId(INITIATE_GENERAL_APPLICATION.name()).build())
                .build();

            Object result = aspect.checkOngoingBusinessProcess(proceedingJoinPoint, callbackParams);

            assertThat(result).isEqualTo(response);
            verify(proceedingJoinPoint).proceed();
        }
    }

    @Nested
    class CamundaEvent {

        @ParameterizedTest
        @EnumSource(value = CaseEvent.class, names = {"INITIATE_GENERAL_APPLICATION", "APPLICATION_PROCEEDS_IN_HERITAGE"})
        @SneakyThrows
        void shouldProceedToMethodInvocation_whenNoOngoingBusinessProcess(CaseEvent event) {
            AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder().build();
            when(proceedingJoinPoint.proceed()).thenReturn(response);

            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_START, GeneralApplicationCaseDataBuilder.builder().build())
                .isGeneralApplicationCase(true)
                .request(CallbackRequest.builder().eventId(event.name()).build())
                .build();

            Object result = aspect.checkOngoingBusinessProcess(proceedingJoinPoint, callbackParams);

            assertThat(result).isEqualTo(response);
            verify(proceedingJoinPoint).proceed();
        }
    }
}
