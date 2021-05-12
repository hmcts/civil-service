package uk.gov.hmcts.reform.civil.stateflow;

import org.assertj.core.api.AbstractAssert;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;

public class StateFlowAssert extends AbstractAssert<StateFlowAssert, StateFlow> {

    private StateFlowAssert(StateFlow actual) {
        super(actual, StateFlowAssert.class);
    }

    public static StateFlowAssert assertThat(StateFlow actual) {
        return new StateFlowAssert(actual);
    }

    StateFlowAssert enteredStates(String... states) {
        isNotNull();

        StateMachineTestPlan<String, String> plan =
            StateMachineTestPlanBuilder.<String, String>builder()
                .stateMachine(actual.asStateMachine())
                .step()
                .expectStateEntered(states)
                .and()
                .build();
        try {
            plan.test();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build StateMachineTestPlan.", e);
        }

        return this;
    }
}
