package uk.gov.hmcts.reform.civil.ga.stateflow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

class GaStateFlowBuilderTest {

    enum FlowState {
        STATE_1,
        STATE_2,
        STATE_3
    }

    enum SubflowState {
        STATE_1,
        STATE_2
    }

    @Nested
    class IllegalArgumentExcpt {

        @Test
        void shouldThrowIllegalArgumentException_whenFlowNameIsNull() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> GaStateFlowBuilder.<FlowState>flow(null));
        }

        @Test
        void shouldThrowIllegalArgumentException_whenFlowNameIsEmpty() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> GaStateFlowBuilder.<FlowState>flow(""));
        }

        @Test
        void shouldThrowIllegalArgumentException_whenSubflowNameIsNull() {
            StateFlowContext stateFlowContext = new StateFlowContext();
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> GaStateFlowBuilder.<SubflowState>subflow(null, stateFlowContext)
            );
        }

        @Test
        void shouldThrowIllegalArgumentException_whenSubflowNameIsEmpty() {
            StateFlowContext stateFlowContext = new StateFlowContext();
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> GaStateFlowBuilder.<SubflowState>subflow("", stateFlowContext)
            );
        }

        @Test
        void shouldThrowIllegalArgumentException_whenStateFlowContextIsNull() {
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> GaStateFlowBuilder.<SubflowState>subflow("SUBFLOW", null)
            );
        }

        @Test
        void shouldThrowIllegalArgumentException_whenInitialIsNull() {
            var flow = GaStateFlowBuilder.<FlowState>flow("FLOW");
            Assertions.assertThrows(IllegalArgumentException.class, () -> flow.initial(null));
        }

        @Test
        void shouldThrowIllegalArgumentException_whenTransitionToIsNull() {
            var flow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1);
            Assertions.assertThrows(IllegalArgumentException.class, () -> flow.transitionTo(null));
        }

        @Test
        void shouldThrowIllegalArgumentException_whenStateIsNull() {
            var flow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_1);
            Assertions.assertThrows(IllegalArgumentException.class, () -> flow.state(null));
        }

        @Test
        void shouldThrowIllegalArgumentException_whenOnlyIfIsNull() {
            var flow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_1);
            Assertions.assertThrows(IllegalArgumentException.class, () -> flow.onlyIf(null));
        }

        @Test
        void shouldThrowIllegalArgumentException_whenSubflowIsNull() {
            var flow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1);
            Assertions.assertThrows(IllegalArgumentException.class, () -> flow.subflow(null));
        }
    }

    @Nested
    class Build {

        @Test
        void shouldBuildStateFlow_whenTransitionIsImplicit() {
            GaStateFlow stateFlow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2)
                .state(FlowState.STATE_2)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_2");
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
        }

        @Test
        void shouldBuildStateFlow_whenTransitionHasTrueCondition() {
            GaStateFlow stateFlow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(caseDetails -> true)
                .state(FlowState.STATE_2)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_2");
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
        }

        @Test
        void shouldBuildStateFlow_whenTransitionHasFalseCondition() {
            GaStateFlow stateFlow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(caseDetails -> false)
                .state(FlowState.STATE_2)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1");
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
        }

        @Test
        void shouldBuildStateFlow_whenTransitionsAreMutuallyExclusive() {
            GaStateFlow stateFlow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(caseDetails -> false)
                .transitionTo(FlowState.STATE_3).onlyIf(caseDetails -> true)
                .state(FlowState.STATE_2)
                .state(FlowState.STATE_3)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_3");
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
        }

        @Test
        void shouldBuildStateFlow_whenTransitionsAreMutuallyExclusiveIncludingImplicitTransitions() {
            GaStateFlow stateFlow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(caseDetails -> false)
                .transitionTo(FlowState.STATE_3)
                .state(FlowState.STATE_2)
                .state(FlowState.STATE_3)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_3");
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
        }

        @Test
        void shouldBuildStateFlow_whenTransitionToMultipleStates() {
            GaStateFlow stateFlow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(caseDetails -> true)
                .state(FlowState.STATE_2)
                .transitionTo(FlowState.STATE_3)
                .state(FlowState.STATE_3)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_2", "FLOW.STATE_3");
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
        }

        @Test
        void shouldBuildStateFlow_whenTransitionToUndefinedState() {
            GaStateFlow stateFlow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(caseDetails -> true)
                .state(FlowState.STATE_2)
                .transitionTo(FlowState.STATE_3)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_2");
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
        }

        @Test
        @Disabled("Subflow currently allows only one final state to transition back to main flow")
        void shouldBuildStateFlow_whenInitialStateHasSubflow() {
            Consumer<StateFlowContext> subflow = stateFlowContext ->
                GaStateFlowBuilder.<SubflowState>subflow("SUBFLOW", stateFlowContext)
                        .transitionTo(SubflowState.STATE_1).onlyIf(caseData -> true)
                        .transitionTo(SubflowState.STATE_2).onlyIf(caseData -> false)
                    .state(SubflowState.STATE_1)
                    .state(SubflowState.STATE_2);

            GaStateFlow stateFlow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .subflow(subflow)
                .transitionTo(FlowState.STATE_2).onlyIf(caseData -> true)
                .state(FlowState.STATE_2)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates(
                "FLOW.STATE_1",
                "SUBFLOW.STATE_1",
                "FLOW.STATE_2"
            );
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
        }

        @Test
        void shouldBuildStateFlow_whenTransitionHasSubflow() {
            Consumer<StateFlowContext> subflow = stateFlowContext ->
                GaStateFlowBuilder.<SubflowState>subflow("SUBFLOW", stateFlowContext)
                    .transitionTo(SubflowState.STATE_1)
                    .state(SubflowState.STATE_1)
                    .transitionTo(SubflowState.STATE_2)
                    .state(SubflowState.STATE_2);

            GaStateFlow stateFlow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2)
                .state(FlowState.STATE_2)
                .subflow(subflow)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates(
                "FLOW.STATE_1",
                "FLOW.STATE_2",
                "SUBFLOW.STATE_1",
                "SUBFLOW.STATE_2"
            );
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
        }

        @Test
        void shouldSetStateMachineError_whenConditionsOnTransitionsAreNotMutuallyExclusive() {
            GaStateFlow stateFlow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(caseDetails -> true)
                .transitionTo(FlowState.STATE_3).onlyIf(caseDetails -> true)
                .state(FlowState.STATE_2)
                .state(FlowState.STATE_3)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_3");
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isTrue();
        }

        @Test
        void shouldSetStateMachineError_whenMoreThanOneTransitionsAreImplicit() {
            GaStateFlow stateFlow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2)
                .transitionTo(FlowState.STATE_3)
                .state(FlowState.STATE_2)
                .state(FlowState.STATE_3)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_3");
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isTrue();
        }

        @Test
        void shouldSetStateMachineError_whenImplicitTransitionAndConditionalTransitionAreNotMutuallyExclusive() {
            GaStateFlow stateFlow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(caseDetails -> true)
                .transitionTo(FlowState.STATE_3)
                .state(FlowState.STATE_2)
                .state(FlowState.STATE_3)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_3");
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isTrue();
        }

        @Test
        void shouldBuildStateFlowWithSubflowButSetStateMachineError_whenAmbiguousTransitions() {
            Consumer<StateFlowContext> subflow = stateFlowContext ->
                GaStateFlowBuilder.<SubflowState>subflow("SUBFLOW", stateFlowContext)
                    .transitionTo(SubflowState.STATE_1)
                    .state(SubflowState.STATE_1);

            GaStateFlow stateFlow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2)
                .subflow(subflow)
                .state(FlowState.STATE_2)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "SUBFLOW.STATE_1");
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isTrue();
        }
    }

    @Nested
    class Evaluate {

        @Test
        void shouldEvaluateStateAndGetStateHistory() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().build();

            Predicate<GeneralApplicationCaseData> firstPredicate = c -> {
                assertThat(c).isSameAs(caseData);
                return true;
            };

            Predicate<GeneralApplicationCaseData> secondPredicate = c -> {
                assertThat(c).isSameAs(caseData);
                return false;
            };

            GaStateFlow stateFlow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(firstPredicate)
                .state(FlowState.STATE_2)
                .transitionTo(FlowState.STATE_3).onlyIf(secondPredicate)
                .state(FlowState.STATE_3)
                .build();

            stateFlow.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isEqualTo("FLOW.STATE_2");

            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly("FLOW.STATE_1", "FLOW.STATE_2");
        }

        @Test
        void shouldEvaluateStateAndFlags() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().build();

            Predicate<GeneralApplicationCaseData> firstPredicate = c -> {
                assertThat(c).isSameAs(caseData);
                return true;
            };

            Predicate<GeneralApplicationCaseData> secondPredicate = c -> {
                assertThat(c).isSameAs(caseData);
                return false;
            };

            GaStateFlow stateFlow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                    .transitionTo(FlowState.STATE_2)
                    .onlyIf(firstPredicate)
                    .set(flags -> flags.put("FIRST_FLAG", true))
                .state(FlowState.STATE_2)
                    .transitionTo(FlowState.STATE_3)
                    .onlyIf(secondPredicate)
                    .set(flags -> flags.put("SECOND_FLAG", true))
                .state(FlowState.STATE_3)
                .build();

            stateFlow.evaluate(caseData);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isEqualTo("FLOW.STATE_2");

            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly("FLOW.STATE_1", "FLOW.STATE_2");

            assertThat(stateFlow.getFlags())
                .contains(entry("FIRST_FLAG", true));
        }

        @Test
        void shouldEvaluateStateAndGetStateHistory_whenAmbiguousTransitions() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().build();

            GaStateFlow stateFlow = GaStateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2)
                .transitionTo(FlowState.STATE_3)
                .state(FlowState.STATE_2)
                .state(FlowState.STATE_3)
                .build();

            stateFlow.evaluate(caseData);

            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly("FLOW.STATE_1", "FLOW.STATE_3");
        }
    }
}
