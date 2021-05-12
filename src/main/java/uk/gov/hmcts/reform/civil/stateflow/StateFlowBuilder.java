package uk.gov.hmcts.reform.civil.stateflow;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.configurers.ExternalTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.stateflow.exception.StateFlowException;
import uk.gov.hmcts.reform.civil.stateflow.grammar.Build;
import uk.gov.hmcts.reform.civil.stateflow.grammar.BuildNext;
import uk.gov.hmcts.reform.civil.stateflow.grammar.CreateFlow;
import uk.gov.hmcts.reform.civil.stateflow.grammar.CreateFlowNext;
import uk.gov.hmcts.reform.civil.stateflow.grammar.CreateSubflow;
import uk.gov.hmcts.reform.civil.stateflow.grammar.CreateSubflowNext;
import uk.gov.hmcts.reform.civil.stateflow.grammar.Initial;
import uk.gov.hmcts.reform.civil.stateflow.grammar.InitialNext;
import uk.gov.hmcts.reform.civil.stateflow.grammar.OnlyIf;
import uk.gov.hmcts.reform.civil.stateflow.grammar.OnlyIfNext;
import uk.gov.hmcts.reform.civil.stateflow.grammar.State;
import uk.gov.hmcts.reform.civil.stateflow.grammar.StateNext;
import uk.gov.hmcts.reform.civil.stateflow.grammar.Subflow;
import uk.gov.hmcts.reform.civil.stateflow.grammar.SubflowNext;
import uk.gov.hmcts.reform.civil.stateflow.grammar.TransitionTo;
import uk.gov.hmcts.reform.civil.stateflow.grammar.TransitionToNext;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.stateflow.StateFlowContext.EXTENDED_STATE_CASE_KEY;

/**
 * DSL for creating a StateFlow which wraps a state engine backed by Spring State Machine.
 * Once created a StateFlow can:
 * - evaluate the current state of a Case
 * - return the internal state engine for further processing
 */
public class StateFlowBuilder<S> {

    private static final String FLOW_NAME = "flowName";
    private static final String STATE = "state";
    // The internal stateFlowContext object. Methods in the DSL work on this
    private final String flowName;
    private final StateFlowContext stateFlowContext;

    private StateFlowBuilder(final String flowName) {
        this.flowName = flowName;
        this.stateFlowContext = new StateFlowContext();
    }

    private StateFlowBuilder(final String flowName, final StateFlowContext stateFlowContext) {
        this.flowName = flowName;
        this.stateFlowContext = stateFlowContext;
    }

    public static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    private static void checkNull(Object object, String name) {
        if (object == null) {
            throw new IllegalArgumentException(name + " may not be null");
        }
    }

    private static void checkEmpty(String string, String name) {
        if (isEmpty(string)) {
            throw new IllegalArgumentException(name + " may not be null or empty string");
        }
    }

    /**
     * Start building a new flow, starting with a FLOW clause.
     *
     * @param flowName name of the flow
     * @return FlowNext which specifies what can come after a FLOW clause
     */
    public static <S> CreateFlowNext<S> flow(String flowName) {
        checkNull(flowName, FLOW_NAME);
        checkEmpty(flowName, FLOW_NAME);
        StateFlowBuilder<S> stateFlowBuilder = new StateFlowBuilder<>(flowName);
        return stateFlowBuilder.flow();
    }

    private CreateFlowNext<S> flow() {
        return new Grammar<>();
    }

    /**
     * Start building a new subflow, starting with a SUBFLOW clause.
     *
     * @param flowName name of the flow
     * @return SubflowNext which specifies what can come after a SUBFLOW clause
     */
    public static <S> CreateSubflowNext<S> subflow(String flowName, StateFlowContext stateFlowContext) {
        checkNull(flowName, FLOW_NAME);
        checkEmpty(flowName, FLOW_NAME);
        checkNull(stateFlowContext, "stateFlowContext");
        StateFlowBuilder<S> stateFlowBuilder = new StateFlowBuilder<>(flowName, stateFlowContext);
        return stateFlowBuilder.subflow();
    }

    private CreateSubflowNext<S> subflow() {
        return new Grammar<>();
    }

    @Override
    public String toString() {
        return stateFlowContext.toString();
    }

    // Grammar
    protected class Grammar<S>
        implements
        CreateFlowNext<S>, CreateFlow<S>,
        CreateSubflowNext<S>, CreateSubflow<S>,
        InitialNext<S>, Initial<S>,
        TransitionToNext<S>, TransitionTo<S>,
        OnlyIfNext<S>, OnlyIf<S>,
        StateNext<S>, State<S>,
        SubflowNext<S>, Subflow<S>,
        BuildNext, Build {

        @Override
        public CreateFlowNext<S> createFlow() {
            return this;
        }

        @Override
        public CreateSubflowNext<S> createSubflow() {
            return this;
        }

        @Override
        public InitialNext<S> initial(S state) {
            return addState(state);
        }

        private Grammar<S> addState(S state) {
            checkNull(state, STATE);
            stateFlowContext.addState(fullyQualified(state));
            return this;
        }

        @Override
        public TransitionToNext<S> transitionTo(S state) {
            checkNull(state, STATE);
            stateFlowContext.getCurrentState()
                .map(currentState -> new Transition(currentState, fullyQualified(state)))
                .ifPresent(stateFlowContext::addTransition);
            return this;
        }

        @Override
        public OnlyIfNext<S> onlyIf(Predicate<CaseData> condition) {
            checkNull(condition, STATE);
            stateFlowContext.getCurrentTransition()
                .ifPresent(currentTransition -> currentTransition.setCondition(condition));
            return this;
        }

        @Override
        public StateNext<S> state(S state) {
            return addState(state);
        }

        @Override
        public SubflowNext<S> subflow(Consumer<StateFlowContext> consumer) {
            checkNull(consumer, "subflow");
            consumer.accept(stateFlowContext);
            return this;
        }

        @Override
        public StateFlow build() {
            StateMachineBuilder.Builder<String, String> stateMachineBuilder =
                StateMachineBuilder.builder();

            try {
                // Config
                stateMachineBuilder.configureConfiguration()
                    .withConfiguration()
                    .autoStartup(false);

                // States
                StateConfigurer<String, String> statesConfigurer =
                    stateMachineBuilder.configureStates().withStates();
                stateFlowContext.getInitialState().ifPresent(statesConfigurer::initial);
                stateFlowContext.getStates().forEach(statesConfigurer::state);

                // Transitions
                for (Transition transition : stateFlowContext.getTransitions()) {
                    ExternalTransitionConfigurer<String, String> transitionConfigurer =
                        stateMachineBuilder.configureTransitions()
                            .withExternal()
                            .source(transition.getSourceState())
                            .target(transition.getTargetState());

                    if (transition.getCondition() != null) {
                        transitionConfigurer.guard(
                            context -> transition.getCondition().test(
                                context.getExtendedState().get(EXTENDED_STATE_CASE_KEY, CaseData.class)
                            )
                        );
                    }
                }
            } catch (Exception e) {
                throw new StateFlowException("Failed to build StateFlow internal state machine.", e);
            }

            // Register listener
            StateMachine<String, String> stateMachine = stateMachineBuilder.build();
            stateMachine.addStateListener(new StateFlowListener());

            return new StateFlow(stateMachine);
        }

        private String fullyQualified(S state) {
            return String.format("%s.%s", flowName, state.toString());
        }
    }
}
