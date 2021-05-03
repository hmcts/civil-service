package uk.gov.hmcts.reform.civil.stateflow.utils;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StateMachineUtils {

    private StateMachineUtils() {
        //Utility class
    }

    private static <S, E> Predicate<Transition<S, E>> isTransitionSourceEqualTo(S state) {
        return transition -> transition.getSource().getId().equals(state);
    }

    private static <S, E> Predicate<Transition<S, E>> isTransitionPermitted(StateContext<S, E> stateContext) {
        return transition -> {
            Function<StateContext<S, E>, Mono<Boolean>> guard = transition.getGuard();
            if (guard == null) {
                return true;
            }
            return (boolean) guard.apply(stateContext).block();
        };
    }

    public static <S, E> Collection<Transition<S, E>> findPermittedTransitionsForState(
        StateContext<S, E> stateContext,
        State<S, E> state
    ) {
        StateMachine<S, E> stateMachine = stateContext.getStateMachine();

        return stateMachine.getTransitions().stream()
            .filter(isTransitionSourceEqualTo(state.getId()))
            .filter(isTransitionPermitted(stateContext))
            .collect(Collectors.toList());
    }
}
