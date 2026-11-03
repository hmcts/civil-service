package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

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
        CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(123L).build();

        InterceptorContext<CaseDetails> context = new InterceptorContext<>("scheduler", searchResultDetails);
        context.setAttribute(CaseInterceptorAttributes.CIVIL_CASE_DATA, caseData);

        when(allowedEventService.isAllowed(caseData, TAKE_CASE_OFFLINE)).thenReturn(true);

        interceptor.accept(context, chain);

        verify(chain).next(context);
        verifyNoInteractions(coreCaseDataService, caseDetailsConverter);
    }

    @Test
    void shouldCallNext_whenTakeCaseOfflineEventIsAllowed_forSpecClaim() {
        CaseDetails searchResultDetails = CaseDetails.builder().id(123L).build();
        CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(123L).caseAccessCategory(SPEC_CLAIM).build();

        InterceptorContext<CaseDetails> context = new InterceptorContext<>("scheduler", searchResultDetails);
        context.setAttribute(CaseInterceptorAttributes.CIVIL_CASE_DATA, caseData);

        when(allowedEventService.isAllowed(caseData, TAKE_CASE_OFFLINE)).thenReturn(true);

        interceptor.accept(context, chain);

        verify(chain).next(context);
        verifyNoInteractions(coreCaseDataService, caseDetailsConverter);
    }

    @Test
    void shouldCallNext_whenTakeCaseOfflineEventIsAllowed_withoutCachedCaseData() {
        CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(123L).build();
        CaseDetails fullCaseDetails = CaseDetails.builder().id(123L).build();

        when(coreCaseDataService.getCase(123L)).thenReturn(fullCaseDetails);
        when(caseDetailsConverter.toCaseData(fullCaseDetails)).thenReturn(caseData);
        when(allowedEventService.isAllowed(caseData, TAKE_CASE_OFFLINE)).thenReturn(true);

        CaseDetails searchResultDetails = CaseDetails.builder().id(123L).build();
        InterceptorContext<CaseDetails> context = new InterceptorContext<>("scheduler", searchResultDetails);

        interceptor.accept(context, chain);

        verify(chain).next(context);
        verify(coreCaseDataService).getCase(123L);
        assertThat(context.getAttribute(CaseInterceptorAttributes.CIVIL_CASE_DATA)).contains(caseData);
    }

    @Test
    void shouldThrowTaskAbortedExceptionAndLog_whenTakeCaseOfflineEventIsNotAllowed() {
        CaseDetails searchResultDetails = CaseDetails.builder().id(123L).state("CASE_ISSUED").build();
        CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(123L).build();

        InterceptorContext<CaseDetails> context = new InterceptorContext<>("scheduler", searchResultDetails);
        context.setAttribute(CaseInterceptorAttributes.CIVIL_CASE_DATA, caseData);

        when(allowedEventService.isAllowed(caseData, TAKE_CASE_OFFLINE)).thenReturn(false);

        StateFlow stateFlow = mock(StateFlow.class);
        State state1 = mock(State.class);
        State state2 = mock(State.class);
        when(state1.getName()).thenReturn("MAIN.DRAFT");
        when(state2.getName()).thenReturn("MAIN.CLAIM_SUBMITTED");
        when(stateFlow.getState()).thenReturn(state2);
        when(stateFlow.getStateHistory()).thenReturn(List.of(state1, state2));
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);

        TaskAbortedException exception = assertThrows(TaskAbortedException.class, () -> interceptor.accept(context, chain));

        assertThat(exception.getReason()).isEqualTo("TAKE_CASE_OFFLINE not allowed in current flow state");
        assertThat(exception.getCategory()).isEqualTo(AbortCategory.FLOW_STATE_DISALLOWED);
        assertThat(context.getProperties()).containsEntry("targetEvent", "TAKE_CASE_OFFLINE");
        assertThat(context.getProperties()).containsEntry("ccdState", "CASE_ISSUED");
        assertThat(context.getProperties()).containsEntry("multiPartyScenario", "ONE_V_ONE");
        assertThat(context.getProperties()).containsEntry("flowState", "MAIN.CLAIM_SUBMITTED");
        assertThat(context.getProperties()).containsEntry("stateFlowHistory", "MAIN.DRAFT, MAIN.CLAIM_SUBMITTED");
        verify(chain, never()).next(context);
        verify(stateFlowEngine).evaluate(caseData);
    }

    @Test
    void shouldThrowTaskAbortedExceptionAndLog_whenTakeCaseOfflineEventIsNotAllowed_forSpecClaim() {
        CaseDetails searchResultDetails = CaseDetails.builder().id(123L).state("AWAITING_RESPONDENT_ACKNOWLEDGEMENT").build();
        CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(123L).caseAccessCategory(SPEC_CLAIM).build();

        InterceptorContext<CaseDetails> context = new InterceptorContext<>("scheduler", searchResultDetails);
        context.setAttribute(CaseInterceptorAttributes.CIVIL_CASE_DATA, caseData);

        when(allowedEventService.isAllowed(caseData, TAKE_CASE_OFFLINE)).thenReturn(false);

        StateFlow stateFlow = mock(StateFlow.class);
        State state = mock(State.class);
        when(state.getName()).thenReturn("MAIN.SPEC_DRAFT");
        when(stateFlow.getState()).thenReturn(state);
        when(stateFlow.getStateHistory()).thenReturn(List.of(state));
        when(stateFlowEngine.evaluateSpec(caseData)).thenReturn(stateFlow);

        TaskAbortedException exception = assertThrows(TaskAbortedException.class, () -> interceptor.accept(context, chain));

        assertThat(exception.getReason()).isEqualTo("TAKE_CASE_OFFLINE not allowed in current flow state");
        assertThat(exception.getCategory()).isEqualTo(AbortCategory.FLOW_STATE_DISALLOWED);
        assertThat(context.getProperties()).containsEntry("targetEvent", "TAKE_CASE_OFFLINE");
        assertThat(context.getProperties()).containsEntry("caseAccessCategory", "SPEC_CLAIM");
        assertThat(context.getProperties()).containsEntry("ccdState", "AWAITING_RESPONDENT_ACKNOWLEDGEMENT");
        assertThat(context.getProperties()).containsEntry("multiPartyScenario", "ONE_V_ONE");
        assertThat(context.getProperties()).containsEntry("flowState", "MAIN.SPEC_DRAFT");
        assertThat(context.getProperties()).containsEntry("stateFlowHistory", "MAIN.SPEC_DRAFT");
        verify(chain, never()).next(context);
        verify(stateFlowEngine).evaluateSpec(caseData);
    }

    @Test
    void shouldThrowTaskAbortedException_whenStateFlowEngineThrowsExceptionDuringLogging() {
        CaseDetails searchResultDetails = CaseDetails.builder().id(123L).state("CASE_ISSUED").build();
        CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(123L).build();

        InterceptorContext<CaseDetails> context = new InterceptorContext<>("scheduler", searchResultDetails);
        context.setAttribute(CaseInterceptorAttributes.CIVIL_CASE_DATA, caseData);

        when(allowedEventService.isAllowed(caseData, TAKE_CASE_OFFLINE)).thenReturn(false);
        when(stateFlowEngine.evaluate(caseData)).thenThrow(new StateFlowException("State flow error"));

        TaskAbortedException exception = assertThrows(TaskAbortedException.class, () -> interceptor.accept(context, chain));

        assertThat(exception.getReason()).isEqualTo("TAKE_CASE_OFFLINE not allowed in current flow state");
        assertThat(exception.getCategory()).isEqualTo(AbortCategory.FLOW_STATE_DISALLOWED);
        assertThat(context.getProperties()).containsEntry("targetEvent", "TAKE_CASE_OFFLINE");
        assertThat(context.getProperties()).containsEntry("ccdState", "CASE_ISSUED");
        assertThat(context.getProperties()).containsEntry("multiPartyScenario", "ONE_V_ONE");
        assertThat(context.getProperties()).doesNotContainKeys("flowState", "stateFlowHistory");
        verify(chain, never()).next(context);
    }

    @Test
    void shouldThrowTaskAbortedExceptionAndLog_whenTakeCaseOfflineEventIsNotAllowed_forTwoVOne() {
        CaseDetails searchResultDetails = CaseDetails.builder().id(123L).state("CASE_ISSUED").build();
        CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(123L).multiPartyClaimTwoApplicants().build();

        InterceptorContext<CaseDetails> context = new InterceptorContext<>("scheduler", searchResultDetails);
        context.setAttribute(CaseInterceptorAttributes.CIVIL_CASE_DATA, caseData);

        when(allowedEventService.isAllowed(caseData, TAKE_CASE_OFFLINE)).thenReturn(false);

        StateFlow stateFlow = mock(StateFlow.class);
        State state = mock(State.class);
        when(state.getName()).thenReturn("MAIN.CLAIM_ISSUED");
        when(stateFlow.getState()).thenReturn(state);
        when(stateFlow.getStateHistory()).thenReturn(List.of(state));
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);

        TaskAbortedException exception = assertThrows(TaskAbortedException.class, () -> interceptor.accept(context, chain));

        assertThat(exception.getReason()).isEqualTo("TAKE_CASE_OFFLINE not allowed in current flow state");
        assertThat(exception.getCategory()).isEqualTo(AbortCategory.FLOW_STATE_DISALLOWED);
        assertThat(context.getProperties()).containsEntry("targetEvent", "TAKE_CASE_OFFLINE");
        assertThat(context.getProperties()).containsEntry("ccdState", "CASE_ISSUED");
        assertThat(context.getProperties()).containsEntry("multiPartyScenario", "TWO_V_ONE");
        assertThat(context.getProperties()).containsEntry("flowState", "MAIN.CLAIM_ISSUED");
        assertThat(context.getProperties()).containsEntry("stateFlowHistory", "MAIN.CLAIM_ISSUED");
        verify(chain, never()).next(context);
        verify(stateFlowEngine).evaluate(caseData);
    }

    @Test
    void shouldReturnOrder100() {
        assertThat(interceptor.getOrder()).isEqualTo(100);
    }
}
