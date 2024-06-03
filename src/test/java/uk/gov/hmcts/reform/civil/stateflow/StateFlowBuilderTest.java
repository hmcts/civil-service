package uk.gov.hmcts.reform.civil.stateflow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.function.Predicate;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

class StateFlowBuilderTest {

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
            Assertions.assertThrows(IllegalArgumentException.class, () -> StateFlowBuilder.<FlowState>flow(null));
        }

        @Test
        void shouldThrowIllegalArgumentException_whenFlowNameIsEmpty() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> StateFlowBuilder.<FlowState>flow(""));
        }

        @Test
        void shouldThrowIllegalArgumentException_whenInitialIsNull() {
            var flow = StateFlowBuilder.<FlowState>flow("FLOW");
            Assertions.assertThrows(IllegalArgumentException.class, () -> flow.initial(null));
        }

        @Test
        void shouldThrowIllegalArgumentException_whenTransitionToIsNull() {
            var flow = StateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1);
            Assertions.assertThrows(IllegalArgumentException.class, () -> flow.transitionTo(null));
        }

        @Test
        void shouldThrowIllegalArgumentException_whenStateIsNull() {
            var flow = StateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_1);
            Assertions.assertThrows(IllegalArgumentException.class, () -> flow.state(null));
        }

        @Test
        void shouldThrowIllegalArgumentException_whenOnlyIfIsNull() {
            var flow = StateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_1);
            Assertions.assertThrows(IllegalArgumentException.class, () -> flow.onlyIf(null));
        }

    }

    @Nested
    class Build {

        @Test
        void shouldBuildStateFlow_whenTransitionIsImplicit() {
            StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2)
                .state(FlowState.STATE_2)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_2");
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
        }

        @Test
        void shouldBuildStateFlow_whenTransitionHasTrueCondition() {
            StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(caseDetails -> true)
                .state(FlowState.STATE_2)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_2");
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
        }

        @Test
        void shouldBuildStateFlow_whenTransitionHasFalseCondition() {
            StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(caseDetails -> false)
                .state(FlowState.STATE_2)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1");
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
        }

        @Test
        void shouldBuildStateFlow_whenTransitionsAreMutuallyExclusive() {
            StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
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
            StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
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
            StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
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
            StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(caseDetails -> true)
                .state(FlowState.STATE_2)
                .transitionTo(FlowState.STATE_3)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_2");
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isFalse();
        }

        @Test
        void shouldSetStateMachineError_whenConditionsOnTransitionsAreNotMutuallyExclusive() {
            StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
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
            StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
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
            StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2).onlyIf(caseDetails -> true)
                .transitionTo(FlowState.STATE_3)
                .state(FlowState.STATE_2)
                .state(FlowState.STATE_3)
                .build();

            StateFlowAssert.assertThat(stateFlow).enteredStates("FLOW.STATE_1", "FLOW.STATE_3");
            assertThat(stateFlow.asStateMachine().hasStateMachineError()).isTrue();
        }
    }

    @Nested
    class Evaluate {

        @Test
        void shouldEvaluateStateAndGetStateHistory() {
            CaseData caseData = CaseData.builder().build();

            Predicate<CaseData> firstPredicate = c -> {
                assertThat(c).isSameAs(caseData);
                return true;
            };

            Predicate<CaseData> secondPredicate = c -> {
                assertThat(c).isSameAs(caseData);
                return false;
            };

            StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
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
            CaseData caseData = CaseData.builder().build();

            Predicate<CaseData> firstPredicate = c -> {
                assertThat(c).isSameAs(caseData);
                return true;
            };

            Predicate<CaseData> secondPredicate = c -> {
                assertThat(c).isSameAs(caseData);
                return false;
            };

            StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
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
        void shouldEvaluateStateAndFlagsDynamicTrue() {
            CaseData caseData = CaseData.builder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .build();

            StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2)
                .onlyIf(c -> true)
                .set((c, flags) -> flags.put("FIRST_FLAG", CaseCategory.UNSPEC_CLAIM.equals(c.getCaseAccessCategory())))
                .state(FlowState.STATE_2)
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
        void shouldEvaluateStateAndFlagsDynamicFalse() {
            CaseData caseData = CaseData.builder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .build();

            StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
                .initial(FlowState.STATE_1)
                .transitionTo(FlowState.STATE_2)
                .onlyIf(c -> true)
                .set((c, flags) -> flags.put("FIRST_FLAG", CaseCategory.SPEC_CLAIM.equals(c.getCaseAccessCategory())))
                .state(FlowState.STATE_2)
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
                .contains(entry("FIRST_FLAG", false));
        }

        @Test
        void shouldEvaluateStateAndGetStateHistory_whenAmbiguousTransitions() {
            CaseData caseData = CaseData.builder().build();

            StateFlow stateFlow = StateFlowBuilder.<FlowState>flow("FLOW")
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
