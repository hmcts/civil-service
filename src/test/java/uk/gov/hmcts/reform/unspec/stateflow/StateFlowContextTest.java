package uk.gov.hmcts.reform.unspec.stateflow;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.unspec.stateflow.model.Transition;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class StateFlowContextTest {

    @Nested
    class GetInitialState {

        @Test
        void shouldReturnEmptyInitialState_whenNoStatesHaveBeenProvided() {
            StateFlowContext stateFlowContext = new StateFlowContext();
            assertThat(stateFlowContext.getInitialState()).isEmpty();
        }
    }

    @Nested
    class GetCurrentState {

        @Test
        void shouldReturnEmptyCurrentState_whenNoStatesHaveBeenProvided() {
            StateFlowContext stateFlowContext = new StateFlowContext();
            assertThat(stateFlowContext.getCurrentState()).isEmpty();
        }

        @Test
        void shouldGetInitialAndCurrentState_whenStatesHaveBeenProvided() {
            StateFlowContext stateFlowContext = new StateFlowContext();
            stateFlowContext.addState("state-1");
            stateFlowContext.addState("state-2");
            assertThat(stateFlowContext.getInitialState()).isNotEmpty();
            assertThat(stateFlowContext.getInitialState()).contains("state-1");
            assertThat(stateFlowContext.getCurrentState()).isNotEmpty();
            assertThat(stateFlowContext.getCurrentState()).contains("state-2");
        }
    }

    @Nested
    class GetCurrentTransition {

        @Test
        void shouldGetCurrentTransition_whenTransitionsHaveBeenProvided() {
            StateFlowContext stateFlowContext = new StateFlowContext();
            Transition transition1 = new Transition("state-1", "state-2", claim -> true);
            Transition transition2 = new Transition("state-2", "state-3", claim -> false);
            stateFlowContext.addTransition(transition1);
            stateFlowContext.addTransition(transition2);

            Optional<Transition> currentTransition = stateFlowContext.getCurrentTransition();
            assertThat(currentTransition).isNotEmpty();
            assertThat(currentTransition.get())
                .extracting(Transition::getSourceState, Transition::getTargetState, Transition::getCondition)
                .doesNotContainNull()
                .containsExactly(
                    transition2.getSourceState(),
                    transition2.getTargetState(),
                    transition2.getCondition()
                );
        }

        @Test
        void shouldGetEmptyCurrentTransition_whenNoTransitionsHaveBeenProvided() {
            StateFlowContext stateFlowContext = new StateFlowContext();
            assertThat(stateFlowContext.getCurrentTransition()).isEmpty();
        }
    }

    @Nested
    class GetStates {

        @Test
        void shouldGetStates_whenStatesHaveBeenProvided() {
            StateFlowContext stateFlowContext = new StateFlowContext();
            stateFlowContext.addState("state-1");
            stateFlowContext.addState("state-2");
            assertThat(stateFlowContext.getStates())
                .hasSize(2)
                .contains("state-1", "state-2");
        }
    }

    @Nested
    class GetTransitions {

        @Test
        void shouldGetTransitions_whenTransitionsHaveBeenProvided() {
            StateFlowContext stateFlowContext = new StateFlowContext();
            Transition transition1 = new Transition("state-1", "state-2", claim -> true);
            Transition transition2 = new Transition("state-2", "state-3", claim -> false);
            stateFlowContext.addTransition(transition1);
            stateFlowContext.addTransition(transition2);

            assertThat(stateFlowContext.getTransitions())
                .hasSize(2)
                .contains(transition1, transition2);
        }
    }
}
