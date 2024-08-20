package uk.gov.hmcts.reform.civil.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowStateAllowedEventService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;

@ExtendWith(MockitoExtension.class)
class EventAllowedAspectTest {

    @InjectMocks
    private EventAllowedAspect eventAllowedAspect;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Mock
    private FlowStateAllowedEventService flowStateAllowedEventService;

    @Mock
    private SimpleStateFlowEngine stateFlowEngine;

    private static final String ERROR_MESSAGE = "This action cannot currently be performed because it has either "
        + "already been completed or another action must be completed first.";

    @ParameterizedTest
    @EnumSource(value = CallbackType.class, mode = EnumSource.Mode.EXCLUDE, names = {"ABOUT_TO_START"})
    void shouldProceed_whenCallbackTypeIsNotAboutToStart(CallbackType callbackType) throws Throwable {
        AboutToStartOrSubmitCallbackResponse response = mockProceedingJoinPoint();
        CallbackParams callbackParams = buildCallbackParams(callbackType, CaseDetailsBuilder.builder().atStateCaseIssued());

        Object result = eventAllowedAspect.checkEventAllowed(proceedingJoinPoint, callbackParams);

        assertThat(result).isEqualTo(response);
        verify(proceedingJoinPoint).proceed();
    }

    @Test
    void shouldNotProceed_whenEventIsNotAllowed() throws Throwable {
        AboutToStartOrSubmitCallbackResponse response = buildErrorResponse();

        StateFlow stateFlow = mock(StateFlow.class);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(flowStateAllowedEventService.isAllowed(any(), any())).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
        CallbackParams callbackParams = buildCallbackParamsWithRequest(CallbackType.ABOUT_TO_START, caseData,
                                                                       DEFENDANT_RESPONSE.name(), CaseDetailsBuilder.builder().atStatePendingClaimIssued());

        Object result = eventAllowedAspect.checkEventAllowed(proceedingJoinPoint, callbackParams);

        assertThat(result).isEqualTo(response);
        verify(proceedingJoinPoint, never()).proceed();
    }

    @Test
    void shouldProceed_whenEventIsAllowed() throws Throwable {
        AboutToStartOrSubmitCallbackResponse response = mockProceedingJoinPoint();
        when(flowStateAllowedEventService.isAllowed(any(), any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
        CallbackParams callbackParams = buildCallbackParamsWithRequest(CallbackType.ABOUT_TO_START, caseData,
                                                                       CLAIMANT_RESPONSE.name(), CaseDetailsBuilder.builder().atStateRespondedToClaim());

        Object result = eventAllowedAspect.checkEventAllowed(proceedingJoinPoint, callbackParams);

        assertThat(result).isEqualTo(response);
        verify(proceedingJoinPoint).proceed();
    }

    private AboutToStartOrSubmitCallbackResponse mockProceedingJoinPoint() throws Throwable {
        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder().build();
        when(proceedingJoinPoint.proceed()).thenReturn(response);
        return response;
    }

    private CallbackParams buildCallbackParams(CallbackType callbackType, CaseDetailsBuilder caseDetailsBuilder) {
        return CallbackParamsBuilder.builder()
            .of(callbackType, caseDetailsBuilder.build())
            .build();
    }

    private CallbackParams buildCallbackParamsWithRequest(CallbackType callbackType, CaseData caseData, String eventId, CaseDetailsBuilder caseDetailsBuilder) {
        return CallbackParamsBuilder.builder()
            .of(callbackType, caseData)
            .request(CallbackRequest.builder()
                         .eventId(eventId)
                         .caseDetails(caseDetailsBuilder.build())
                         .build())
            .build();
    }

    private AboutToStartOrSubmitCallbackResponse buildErrorResponse() {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(List.of(ERROR_MESSAGE))
            .build();
    }
}
