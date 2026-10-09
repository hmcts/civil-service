package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.flowstate.AllowedEventService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.exception.StateFlowException;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TAKE_CASE_OFFLINE;

@ExtendWith(MockitoExtension.class)
class AllowedEventFlowStateCheckTest {

    @Mock
    private AllowedEventService allowedEventService;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private InterceptorChain<CaseDetails> chain;

    private AllowedEventFlowStateCheck interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new AllowedEventFlowStateCheck(
            allowedEventService,
            stateFlowEngine,
            caseDetailsConverter,
            coreCaseDataService,
            TAKE_CASE_OFFLINE
        );
    }

    @Test
    void shouldCallNext_whenTakeCaseOfflineEventIsAllowed_withCachedCaseData() {
        CaseDetails searchResultDetails = CaseDetails.builder().id(123L).build();
        CaseData caseData = CaseData.builder().ccdCaseReference(123L).build();

        InterceptorContext<CaseDetails> context = new InterceptorContext<>("scheduler", searchResultDetails);
        context.setAttribute(CaseInterceptorAttributes.CIVIL_CASE_DATA, caseData);

        when(allowedEventService.isAllowed(caseData, TAKE_CASE_OFFLINE)).thenReturn(true);

        interceptor.accept(context, chain);

        verify(chain).next(context);
        verifyNoInteractions(coreCaseDataService, caseDetailsConverter);
    }

    @Test
    void shouldCallNext_whenTakeCaseOfflineEventIsAllowed_withoutCachedCaseData() {
        CaseData caseData = CaseData.builder().ccdCaseReference(123L).build();
        CaseDetails fullCaseDetails = CaseDetails.builder().id(123L).build();

        when(coreCaseDataService.getCase(123L)).thenReturn(fullCaseDetails);
        when(caseDetailsConverter.toCaseData(fullCaseDetails)).thenReturn(caseData);
        when(allowedEventService.isAllowed(caseData, TAKE_CASE_OFFLINE)).thenReturn(true);

        CaseDetails searchResultDetails = CaseDetails.builder().id(123L).build();
        InterceptorContext<CaseDetails> context = new InterceptorContext<>("scheduler", searchResultDetails);

        interceptor.accept(context, chain);

        verify(chain).next(context);
        verify(coreCaseDataService).getCase(123L);
    }

    @Test
    void shouldThrowTaskAbortedExceptionAndLog_whenTakeCaseOfflineEventIsNotAllowed() {
        CaseDetails searchResultDetails = CaseDetails.builder().id(123L).build();
        CaseData caseData = CaseData.builder().ccdCaseReference(123L).build();

        InterceptorContext<CaseDetails> context = new InterceptorContext<>("scheduler", searchResultDetails);
        context.setAttribute(CaseInterceptorAttributes.CIVIL_CASE_DATA, caseData);

        when(allowedEventService.isAllowed(caseData, TAKE_CASE_OFFLINE)).thenReturn(false);

        StateFlow stateFlow = mock(StateFlow.class);
        State state = mock(State.class);
        when(state.getName()).thenReturn("MAIN.DRAFT");
        when(stateFlow.getState()).thenReturn(state);
        when(stateFlow.getStateHistory()).thenReturn(List.of(state));
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);

        TaskAbortedException exception = assertThrows(TaskAbortedException.class, () -> interceptor.accept(context, chain));

        assertThat(exception.getReason()).isEqualTo("TAKE_CASE_OFFLINE not allowed in current flow state");
        verify(chain, never()).next(context);
        verify(stateFlowEngine).evaluate(caseData);
    }

    @Test
    void shouldThrowTaskAbortedException_whenStateFlowEngineThrowsExceptionDuringLogging() {
        CaseDetails searchResultDetails = CaseDetails.builder().id(123L).build();
        CaseData caseData = CaseData.builder().ccdCaseReference(123L).build();

        InterceptorContext<CaseDetails> context = new InterceptorContext<>("scheduler", searchResultDetails);
        context.setAttribute(CaseInterceptorAttributes.CIVIL_CASE_DATA, caseData);

        when(allowedEventService.isAllowed(caseData, TAKE_CASE_OFFLINE)).thenReturn(false);
        when(stateFlowEngine.evaluate(caseData)).thenThrow(new StateFlowException("State flow error"));

        TaskAbortedException exception = assertThrows(TaskAbortedException.class, () -> interceptor.accept(context, chain));

        assertThat(exception.getReason()).isEqualTo("TAKE_CASE_OFFLINE not allowed in current flow state");
        verify(chain, never()).next(context);
    }

    @Test
    void shouldReturnOrder100() {
        assertThat(interceptor.getOrder()).isEqualTo(100);
    }
}
