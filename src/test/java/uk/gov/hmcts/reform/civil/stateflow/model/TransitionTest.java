package uk.gov.hmcts.reform.civil.stateflow.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransitionTest {

    @Test
    void shouldConstructAnInstance_whenStatesSpecified() {
        Transition transition = new Transition(
            "state-1",
            "state-2"
        );
        assertThat(transition)
            .extracting(Transition::getSourceState, Transition::getTargetState)
            .doesNotContainNull()
            .containsExactly(transition.getSourceState(), transition.getTargetState());
        assertThat(transition.getCondition())
            .isNull();
        assertThat(transition.getFlags())
            .isNull();
    }

    @Test
    void shouldConstructAnInstance_whenStatesAndConditionIsSpecified() {
        Transition transition = new Transition(
            "state-1",
            "state-2",
            caseDetails -> true
        );
        assertThat(transition)
            .extracting(Transition::getSourceState, Transition::getTargetState, Transition::getCondition)
            .doesNotContainNull()
            .containsExactly(transition.getSourceState(), transition.getTargetState(), transition.getCondition());
        assertThat(transition.getFlags())
            .isNull();
    }

    @Test
    void shouldConstructAnInstance_whenStatesAndConditionAndFlagsIsSpecified() {
        Transition transition = new Transition(
            "state-1",
            "state-2",
            caseDetails -> true,
            flags -> flags.put("KEY", true)
        );
        assertThat(transition)
            .extracting(Transition::getSourceState, Transition::getTargetState, Transition::getCondition, Transition::getFlags)
            .doesNotContainNull()
            .containsExactly(transition.getSourceState(), transition.getTargetState(), transition.getCondition(), transition.getFlags());
    }
}
