package uk.gov.hmcts.reform.civil.service.flowstate;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.repository.AllowedEventRepository;
import uk.gov.hmcts.reform.civil.service.flowstate.scenario.AllowedEventScenario;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_CASE_NOTE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_LIP_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

class AllowedEventServiceTest {

    @Test
    void returnsTrue_whenWhitelistEvent() {
        AllowedEventRepository repo = mock(AllowedEventRepository.class);
        IStateFlowEngine engine = mock(IStateFlowEngine.class);
        CaseDetailsConverter converter = mock(CaseDetailsConverter.class);
        AllowedEventScenario scenario = mock(AllowedEventScenario.class);
        AllowedEventService service = new AllowedEventService(repo, engine, converter, List.of(scenario));
        CaseEvent caseEvent = ADD_CASE_NOTE;

        when(repo.getWhitelist()).thenReturn(Set.of(caseEvent));

        boolean allowed = service.isAllowed(mock(CaseDetails.class), caseEvent);
        assertThat(allowed).isTrue();

        verifyNoInteractions(engine, scenario);
    }

    @Test
    void returnsFalse_whenNoScenarioApplies() {
        AllowedEventRepository repo = mock(AllowedEventRepository.class);
        IStateFlowEngine engine = mock(IStateFlowEngine.class);
        CaseDetailsConverter converter = mock(CaseDetailsConverter.class);
        AllowedEventScenario scenario = mock(AllowedEventScenario.class);
        AllowedEventService service = new AllowedEventService(repo, engine, converter, List.of(scenario));

        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = mock(CaseData.class);
        when(scenario.appliesTo(false)).thenReturn(false);
        when(converter.toCaseData(caseDetails)).thenReturn(caseData);

        boolean allowed = service.isAllowed(caseDetails, DEFENDANT_RESPONSE);
        assertThat(allowed).isFalse();

        verifyNoInteractions(engine);
    }

    @Test
    void usesEvaluateSpec_whenCaseIsSpec() {
        AllowedEventRepository repo = mock(AllowedEventRepository.class);
        IStateFlowEngine engine = mock(IStateFlowEngine.class);
        CaseDetailsConverter converter = mock(CaseDetailsConverter.class);
        AllowedEventScenario scenario = mock(AllowedEventScenario.class);
        AllowedEventService service = new AllowedEventService(repo, engine, converter, List.of(scenario));
        CaseEvent caseEvent = DEFENDANT_RESPONSE;

        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = mock(CaseData.class);

        when(converter.toCaseData(caseDetails)).thenReturn(caseData);
        when(scenario.appliesTo(true)).thenReturn(true);
        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);

        StateFlow stateFlow = mock(StateFlow.class);
        when(engine.evaluateSpec(caseDetails)).thenReturn(stateFlow);

        State state = mock(State.class);
        when(stateFlow.getState()).thenReturn(state);
        FlowState.Main flowState = FlowState.Main.DRAFT;
        when(state.getName()).thenReturn(flowState.fullName());
        when(scenario.loadBaseEvents(flowState.fullName())).thenReturn(Set.of(caseEvent));

        boolean allowed = service.isAllowed(caseDetails, caseEvent);
        assertThat(allowed).isTrue();

        verify(engine).evaluateSpec(caseDetails);
        verify(engine, never()).evaluate(any(CaseDetails.class));
    }

    @Test
    void usesEvaluateDefaultUnspec_whenCaseCategoryIsNull() {
        AllowedEventRepository repo = mock(AllowedEventRepository.class);
        IStateFlowEngine engine = mock(IStateFlowEngine.class);
        CaseDetailsConverter converter = mock(CaseDetailsConverter.class);
        AllowedEventScenario scenario = mock(AllowedEventScenario.class);
        AllowedEventService service = new AllowedEventService(repo, engine, converter, List.of(scenario));
        CaseEvent caseEvent = CREATE_CLAIM;

        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = mock(CaseData.class);

        when(converter.toCaseData(caseDetails)).thenReturn(caseData);
        when(scenario.appliesTo(false)).thenReturn(true);
        when(caseData.getCaseAccessCategory()).thenReturn(null);

        StateFlow stateFlow = mock(StateFlow.class);
        when(engine.evaluate(caseDetails)).thenReturn(stateFlow);

        State state = mock(State.class);
        when(stateFlow.getState()).thenReturn(state);
        FlowState.Main flowState = FlowState.Main.DRAFT;
        when(state.getName()).thenReturn(flowState.fullName());
        when(scenario.loadBaseEvents(flowState.fullName())).thenReturn(Set.of(caseEvent));

        boolean allowed = service.isAllowed(caseDetails, caseEvent);
        assertThat(allowed).isTrue();

        verify(engine).evaluate(caseDetails);
        verify(engine, never()).evaluateSpec(any(CaseDetails.class));
    }

    @Test
    void usesEvaluateSpec_whenEventIsCreateClaimSpec_evenIfCaseIsUnspec() {
        AllowedEventRepository repo = mock(AllowedEventRepository.class);
        IStateFlowEngine engine = mock(IStateFlowEngine.class);
        CaseDetailsConverter converter = mock(CaseDetailsConverter.class);
        AllowedEventScenario scenario = mock(AllowedEventScenario.class);
        AllowedEventService service = new AllowedEventService(repo, engine, converter, List.of(scenario));
        CaseEvent caseEvent = CREATE_CLAIM_SPEC;

        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = mock(CaseData.class);

        when(converter.toCaseData(caseDetails)).thenReturn(caseData);
        when(scenario.appliesTo(true)).thenReturn(true);
        when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);

        StateFlow stateFlow = mock(StateFlow.class);
        when(engine.evaluateSpec(caseDetails)).thenReturn(stateFlow);

        State state = mock(State.class);
        when(stateFlow.getState()).thenReturn(state);
        FlowState.Main flowState = FlowState.Main.DRAFT;
        when(state.getName()).thenReturn(flowState.fullName());
        when(scenario.loadBaseEvents(flowState.fullName())).thenReturn(Set.of(caseEvent));

        boolean allowed = service.isAllowed(caseDetails, caseEvent);
        assertThat(allowed).isTrue();

        verify(engine).evaluateSpec(caseDetails);
        verify(engine, never()).evaluate(any(CaseDetails.class));
    }

    @Test
    void usesEvaluate_whenNotSpecAndEventNotSpecOrLip() {
        AllowedEventRepository repo = mock(AllowedEventRepository.class);
        IStateFlowEngine engine = mock(IStateFlowEngine.class);
        CaseDetailsConverter converter = mock(CaseDetailsConverter.class);
        AllowedEventScenario scenario = mock(AllowedEventScenario.class);
        AllowedEventService service = new AllowedEventService(repo, engine, converter, List.of(scenario));

        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = mock(CaseData.class);
        CaseEvent caseEvent = DEFENDANT_RESPONSE;

        when(converter.toCaseData(caseDetails)).thenReturn(caseData);
        when(scenario.appliesTo(false)).thenReturn(true);
        when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);

        StateFlow stateFlow = mock(StateFlow.class);
        when(engine.evaluate(caseDetails)).thenReturn(stateFlow);

        State state = mock(State.class);
        when(stateFlow.getState()).thenReturn(state);
        FlowState.Main flowState = FlowState.Main.DRAFT;
        when(state.getName()).thenReturn(flowState.fullName());
        when(scenario.loadBaseEvents(flowState.fullName())).thenReturn(Set.of(caseEvent));

        boolean allowed = service.isAllowed(caseDetails, caseEvent);
        assertThat(allowed).isTrue();

        verify(engine).evaluate(caseDetails);
        verify(engine, never()).evaluateSpec(any(CaseDetails.class));
    }

    @Test
    void usesEvaluate_whenNotSpecAndEventSpecOrLip() {
        AllowedEventRepository repo = mock(AllowedEventRepository.class);
        IStateFlowEngine engine = mock(IStateFlowEngine.class);
        CaseDetailsConverter converter = mock(CaseDetailsConverter.class);
        AllowedEventScenario scenario = mock(AllowedEventScenario.class);
        AllowedEventService service = new AllowedEventService(repo, engine, converter, List.of(scenario));

        CaseDetails caseDetails = mock(CaseDetails.class);
        CaseData caseData = mock(CaseData.class);
        CaseEvent caseEvent = CREATE_LIP_CLAIM;

        when(converter.toCaseData(caseDetails)).thenReturn(caseData);
        when(scenario.appliesTo(true)).thenReturn(true);
        when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);

        StateFlow stateFlow = mock(StateFlow.class);
        when(engine.evaluateSpec(caseDetails)).thenReturn(stateFlow);

        State state = mock(State.class);
        when(stateFlow.getState()).thenReturn(state);
        FlowState.Main flowState = FlowState.Main.DRAFT;
        when(state.getName()).thenReturn(flowState.fullName());
        when(scenario.loadBaseEvents(flowState.fullName())).thenReturn(Set.of(caseEvent));

        boolean allowed = service.isAllowed(caseDetails, caseEvent);
        assertThat(allowed).isTrue();

        verify(engine).evaluateSpec(caseDetails);
        verify(engine, never()).evaluate(any(CaseDetails.class));
    }

}
