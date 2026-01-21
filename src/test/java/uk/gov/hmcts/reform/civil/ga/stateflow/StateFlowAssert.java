package uk.gov.hmcts.reform.civil.ga.stateflow;

import org.assertj.core.api.AbstractAssert;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;

public class StateFlowAssert extends AbstractAssert<StateFlowAssert, GaStateFlow> {

    private StateFlowAssert(GaStateFlow actual) {
        super(actual, StateFlowAssert.class);
    }

    public static StateFlowAssert assertThat(GaStateFlow actual) {
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
