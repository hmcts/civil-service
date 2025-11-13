package uk.gov.hmcts.reform.civil.service.flowstate;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.repository.AllowedEventRepository;
import uk.gov.hmcts.reform.civil.service.flowstate.scenario.AllowedEventScenario;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

class AllowedEventServiceTest {

    @Test
    void returnsTrue_whenWhitelistEvent() {
        AllowedEventRepository repo = mock(AllowedEventRepository.class);
        IStateFlowEngine engine = mock(IStateFlowEngine.class);
        AllowedEventScenario scenario = mock(AllowedEventScenario.class);
        AllowedEventService service = new AllowedEventService(repo, engine, List.of(scenario));

        when(repo.getWhitelist()).thenReturn(Set.of(CaseEvent.ADD_CASE_NOTE));

        boolean allowed = service.isAllowed(mock(CaseData.class), CaseEvent.ADD_CASE_NOTE);
        assertThat(allowed).isTrue();
        verifyNoInteractions(engine, scenario);
    }

    @Test
    void returnsFalse_whenNoScenarioApplies() {
        AllowedEventRepository repo = mock(AllowedEventRepository.class);
        IStateFlowEngine engine = mock(IStateFlowEngine.class);
        AllowedEventScenario scenario = mock(AllowedEventScenario.class);
        AllowedEventService service = new AllowedEventService(repo, engine, List.of(scenario));

        CaseData caseData = mock(CaseData.class);
        when(scenario.appliesTo(caseData)).thenReturn(false);

        boolean allowed = service.isAllowed(caseData, DEFENDANT_RESPONSE);
        assertThat(allowed).isFalse();
        verifyNoInteractions(engine);
    }

    @Test
    void usesEvaluateSpec_whenCaseIsSpec() {
        AllowedEventRepository repo = mock(AllowedEventRepository.class);
        IStateFlowEngine engine = mock(IStateFlowEngine.class);
        AllowedEventScenario scenario = mock(AllowedEventScenario.class);
        AllowedEventService service = new AllowedEventService(repo, engine, List.of(scenario));

        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);

        StateFlow stateFlow = mock(StateFlow.class);
        var state = mock(uk.gov.hmcts.reform.civil.stateflow.model.State.class);
        when(state.getName()).thenReturn("MAIN.DRAFT");
        when(stateFlow.getState()).thenReturn(state);
        when(engine.evaluateSpec(caseData)).thenReturn(stateFlow);

        when(scenario.appliesTo(caseData)).thenReturn(true);
        when(scenario.loadBaseEvents("MAIN.DRAFT")).thenReturn(Set.of(DEFENDANT_RESPONSE));

        boolean allowed = service.isAllowed(caseData, DEFENDANT_RESPONSE);
        assertThat(allowed).isTrue();
        verify(engine).evaluateSpec(caseData);
        verify(engine, never()).evaluate(any(CaseData.class));
    }

    @Test
    void usesEvaluateSpec_whenEventIsCreateClaimSpec_evenIfCaseIsUnspec() {
        AllowedEventRepository repo = mock(AllowedEventRepository.class);
        IStateFlowEngine engine = mock(IStateFlowEngine.class);
        AllowedEventScenario scenario = mock(AllowedEventScenario.class);
        AllowedEventService service = new AllowedEventService(repo, engine, List.of(scenario));

        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);

        StateFlow stateFlow = mock(StateFlow.class);
        var state = mock(uk.gov.hmcts.reform.civil.stateflow.model.State.class);
        when(state.getName()).thenReturn("MAIN.DRAFT");
        when(stateFlow.getState()).thenReturn(state);
        when(engine.evaluateSpec(caseData)).thenReturn(stateFlow);

        when(scenario.appliesTo(caseData)).thenReturn(true);
        when(scenario.loadBaseEvents("MAIN.DRAFT")).thenReturn(Set.of(CREATE_CLAIM_SPEC));

        boolean allowed = service.isAllowed(caseData, CREATE_CLAIM_SPEC);
        assertThat(allowed).isTrue();
        verify(engine).evaluateSpec(caseData);
        verify(engine, never()).evaluate(any(CaseData.class));
    }

    @Test
    void usesEvaluate_whenNotSpecAndEventNotSpecOrLip() {
        AllowedEventRepository repo = mock(AllowedEventRepository.class);
        IStateFlowEngine engine = mock(IStateFlowEngine.class);
        AllowedEventScenario scenario = mock(AllowedEventScenario.class);
        AllowedEventService service = new AllowedEventService(repo, engine, List.of(scenario));

        CaseData caseData = mock(CaseData.class);
        when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);

        StateFlow stateFlow = mock(StateFlow.class);
        var state = mock(uk.gov.hmcts.reform.civil.stateflow.model.State.class);
        when(state.getName()).thenReturn("MAIN.CLAIM_ISSUED");
        when(stateFlow.getState()).thenReturn(state);
        when(engine.evaluate(caseData)).thenReturn(stateFlow);

        when(scenario.appliesTo(caseData)).thenReturn(true);
        when(scenario.loadBaseEvents("MAIN.CLAIM_ISSUED")).thenReturn(Set.of(DEFENDANT_RESPONSE));

        boolean allowed = service.isAllowed(caseData, DEFENDANT_RESPONSE);
        assertThat(allowed).isTrue();
        verify(engine).evaluate(caseData);
        verify(engine, never()).evaluateSpec(any(CaseData.class));
    }

}
