package uk.gov.hmcts.reform.civil.stateflow.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class StateMachineUtilsTest {

    @Mock
    private StateContext<String, String> mockedStateContext;

    @Mock
    private State<String, String> mockedState;

    @SuppressWarnings("unchecked")
    private Mono<Boolean> createMockedMono(boolean bool) {
        Mono<Boolean> mockedMono = mock(Mono.class);
        when(mockedMono.block()).thenReturn(bool);
        return mockedMono;
    }

    @SuppressWarnings("unchecked")
    private State<String, String> createMockedState(String state) {
        State<String, String> mockedState = mock(State.class);
        when(mockedState.getId()).thenReturn(state);
        return mockedState;
    }

    @SuppressWarnings("unchecked")
    private Function<StateContext<String, String>, Mono<Boolean>> createMockedGuard(boolean permitted) {
        Function<StateContext<String, String>, Mono<Boolean>> mockedGuard = mock(Function.class);
        Mono<Boolean> mockedMono = createMockedMono(permitted);
        when(mockedGuard.apply(Mockito.any())).thenReturn(mockedMono);
        return mockedGuard;
    }

    @SuppressWarnings("unchecked")
    private Transition<String, String> createMockedGuardedTransition(String state, boolean permitted) {
        Transition<String, String> mockedTransition = mock(Transition.class);
        State<String, String> mockedState = createMockedState(state);
        Function<StateContext<String, String>, Mono<Boolean>> mockedGuard = createMockedGuard(permitted);
        when(mockedTransition.getSource()).thenReturn(mockedState);
        when(mockedTransition.getGuard()).thenReturn(mockedGuard);
        return mockedTransition;
    }

    @SuppressWarnings("unchecked")
    private Transition<String, String> createMockedImplicitTransition(String state) {
        Transition<String, String> mockedTransition = mock(Transition.class);
        State<String, String> mockedState = createMockedState(state);
        when(mockedTransition.getSource()).thenReturn(mockedState);
        when(mockedTransition.getGuard()).thenReturn(null);
        return mockedTransition;
    }

    @SuppressWarnings("unchecked")
    private StateMachine<String, String> createMockedStateMachine(Transition<String, String> transition) {
        StateMachine<String, String> mockedStateMachine = mock(StateMachine.class);
        when(mockedStateMachine.getTransitions()).thenReturn(Collections.singletonList(transition));
        return mockedStateMachine;
    }

    @Test
    void shouldFindPermittedTransitionsForState_whenHasNoTransitions() {
        Transition<String, String> mockedTransition = createMockedGuardedTransition("ANOTHER_STATE", true);
        StateMachine<String, String> mockedStateMachine = createMockedStateMachine(mockedTransition);

        when(mockedStateContext.getStateMachine()).thenReturn(mockedStateMachine);
        when(mockedState.getId()).thenReturn("A_STATE");

        assertThat(StateMachineUtils.findPermittedTransitionsForState(mockedStateContext, mockedState))
            .isEmpty();
    }

    @Test
    void shouldFindPermittedTransitionsForState_whenHasGuardedTransitionsButNoneArePermitted() {
        Transition<String, String> mockedTransition = createMockedGuardedTransition("A_STATE", false);
        StateMachine<String, String> mockedStateMachine = createMockedStateMachine(mockedTransition);

        when(mockedStateContext.getStateMachine()).thenReturn(mockedStateMachine);
        when(mockedState.getId()).thenReturn("A_STATE");

        assertThat(StateMachineUtils.findPermittedTransitionsForState(mockedStateContext, mockedState))
            .isEmpty();
    }

    @Test
    void shouldFindPermittedTransitionsForState_whenHasGuardedTransitionsThatArePermitted() {
        Transition<String, String> mockedTransition = createMockedGuardedTransition("A_STATE", true);
        StateMachine<String, String> mockedStateMachine = createMockedStateMachine(mockedTransition);

        when(mockedStateContext.getStateMachine()).thenReturn(mockedStateMachine);
        when(mockedState.getId()).thenReturn("A_STATE");

        assertThat(StateMachineUtils.findPermittedTransitionsForState(mockedStateContext, mockedState))
            .hasSize(1)
            .containsOnly(mockedTransition);
    }

    @Test
    void shouldFindPermittedTransitionsForState_whenHasImplicitTransitions() {
        Transition<String, String> mockedTransition = createMockedImplicitTransition("A_STATE");
        StateMachine<String, String> mockedStateMachine = createMockedStateMachine(mockedTransition);

        when(mockedStateContext.getStateMachine()).thenReturn(mockedStateMachine);
        when(mockedState.getId()).thenReturn("A_STATE");

        assertThat(StateMachineUtils.findPermittedTransitionsForState(mockedStateContext, mockedState))
            .hasSize(1)
            .containsOnly(mockedTransition);
    }
}
