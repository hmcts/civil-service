package uk.gov.hmcts.reform.civil.stateflow;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.configurers.ExternalTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.stateflow.exception.StateFlowException;
import uk.gov.hmcts.reform.civil.stateflow.grammar.Build;
import uk.gov.hmcts.reform.civil.stateflow.grammar.CreateFlow;
import uk.gov.hmcts.reform.civil.stateflow.grammar.CreateFlowNext;
import uk.gov.hmcts.reform.civil.stateflow.grammar.Initial;
import uk.gov.hmcts.reform.civil.stateflow.grammar.InitialNext;
import uk.gov.hmcts.reform.civil.stateflow.grammar.OnlyIf;
import uk.gov.hmcts.reform.civil.stateflow.grammar.OnlyIfNext;
import uk.gov.hmcts.reform.civil.stateflow.grammar.Set;
import uk.gov.hmcts.reform.civil.stateflow.grammar.SetNext;
import uk.gov.hmcts.reform.civil.stateflow.grammar.State;
import uk.gov.hmcts.reform.civil.stateflow.grammar.StateNext;
import uk.gov.hmcts.reform.civil.stateflow.grammar.TransitionTo;
import uk.gov.hmcts.reform.civil.stateflow.grammar.TransitionToNext;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.stateflow.StateFlowContext.EXTENDED_STATE_CASE_KEY;
import static uk.gov.hmcts.reform.civil.stateflow.StateFlowContext.EXTENDED_STATE_FLAGS_KEY;

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

    @Override
    public String toString() {
        return stateFlowContext.toString();
    }

    // Grammar
    protected class Grammar<S>
        implements
        CreateFlowNext<S>, CreateFlow<S>,
        InitialNext<S>, Initial<S>,
        TransitionToNext<S>, TransitionTo<S>,
        OnlyIfNext<S>, OnlyIf<S>,
        SetNext<S>, Set<S>,
        StateNext<S>, State<S>, Build<S> {

        @Override
        public CreateFlowNext<S> createFlow() {
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
        public SetNext<S> set(Consumer<Map<String, Boolean>> flags) {
            checkNull(flags, STATE);
            stateFlowContext.getCurrentTransition()
                .ifPresent(currentTransition -> currentTransition.setFlags(flags));
            return this;
        }

        @Override
        public SetNext<S> set(BiConsumer<CaseData, Map<String, Boolean>> flags) {
            checkNull(flags, STATE);
            stateFlowContext.getCurrentTransition()
                .ifPresent(currentTransition -> currentTransition.setDynamicFlags(flags));
            return this;
        }

        @Override
        public StateNext<S> state(S state) {
            return addState(state);
        }

        @Override
        @SuppressWarnings("unchecked")
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

                    if (transition.getFlags() != null) {
                        transitionConfigurer.action(
                            action -> transition.getFlags().accept(
                                (Map<String, Boolean>) action.getExtendedState().get(EXTENDED_STATE_FLAGS_KEY, Map.class)
                            )
                        );
                    }

                    if (transition.getDynamicFlags() != null) {
                        transitionConfigurer.action(
                            action -> transition.getDynamicFlags().accept(
                                action.getExtendedState().get(EXTENDED_STATE_CASE_KEY, CaseData.class),
                                (Map<String, Boolean>) action.getExtendedState().get(EXTENDED_STATE_FLAGS_KEY, Map.class)
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
