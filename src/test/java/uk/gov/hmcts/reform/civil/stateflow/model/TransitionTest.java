package uk.gov.hmcts.reform.civil.stateflow.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransitionTest {

    @Test
    void shouldConstructAnInstance_whenNoConditionIsSpecified() {
        Transition transition = new Transition("state-1", "state-2");
        assertThat(transition)
            .extracting(Transition::getSourceState, Transition::getTargetState)
            .doesNotContainNull()
            .containsExactly(transition.getSourceState(), transition.getTargetState());
        assertThat(transition.getCondition())
            .isNull();
    }

    @Test
    void shouldConstructAnInstance_whenConditionIsSpecified() {
        Transition transition = new Transition("state-1", "state-2", caseDatails -> true);
        assertThat(transition)
            .extracting(Transition::getSourceState, Transition::getTargetState, Transition::getCondition)
            .doesNotContainNull()
            .containsExactly(transition.getSourceState(), transition.getTargetState(), transition.getCondition());
    }
}
