package uk.gov.hmcts.reform.civil.stateflow.simplegrammar;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.configurers.ExternalTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.StateFlowContext;
import uk.gov.hmcts.reform.civil.stateflow.StateFlowListener;
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
@Component
public class SimpleStateFlowBuilder {

    private static final String FLOW_NAME = "flowName";
    private static final String STATE = "state";
    // The internal stateFlowContext object. Methods in the DSL work on this
    private final String flowName;

    private final StateFlowContext stateFlowContext;


    public SimpleStateFlowBuilder() {
        this.stateFlowContext = new StateFlowContext();
        this.flowName = "MAIN";

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
    public CreateFlowNext<FlowState.Main> flow(String flowName) {
        checkNull(flowName, FLOW_NAME);
        checkEmpty(flowName, FLOW_NAME);
        SimpleStateFlowBuilder stateFlowBuilder = new SimpleStateFlowBuilder();
        return stateFlowBuilder.flow();
    }

    private CreateFlowNext<FlowState.Main> flow() {
        return new Grammar();
    }

    @Override
    public String toString() {
        return stateFlowContext.toString();
    }

    // Grammar
    protected class Grammar
        implements
        CreateFlowNext<FlowState.Main>, CreateFlow<FlowState.Main>,
        InitialNext<FlowState.Main>, Initial<FlowState.Main>,
        TransitionToNext<FlowState.Main>, TransitionTo<FlowState.Main>,
        OnlyIfNext<FlowState.Main>, OnlyIf<FlowState.Main>,
        SetNext<FlowState.Main>, Set<FlowState.Main>,
        StateNext<FlowState.Main>, State<FlowState.Main>, Build<FlowState.Main> {

        @Override
        public CreateFlowNext<FlowState.Main> createFlow() {
            return this;
        }

        @Override
        public InitialNext<FlowState.Main> initial(FlowState.Main state) {
            return addState(state);
        }

        private Grammar addState(FlowState.Main state) {
            checkNull(state, STATE);
            stateFlowContext.addState(fullyQualified(state));
            return this;
        }

        @Override
        public TransitionToNext<FlowState.Main> transitionTo(FlowState.Main state) {
            checkNull(state, STATE);
            stateFlowContext.getCurrentState()
                .map(currentState -> new Transition(currentState, fullyQualified(state)))
                .ifPresent(stateFlowContext::addTransition);
            return this;
        }

        @Override
        public OnlyIfNext<FlowState.Main> onlyIf(Predicate<CaseData> condition) {
            checkNull(condition, STATE);
            stateFlowContext.getCurrentTransition()
                .ifPresent(currentTransition -> currentTransition.setCondition(condition));
            return this;
        }

        @Override
        public SetNext<FlowState.Main> set(Consumer<Map<String, Boolean>> flags) {
            checkNull(flags, STATE);
            stateFlowContext.getCurrentTransition()
                .ifPresent(currentTransition -> currentTransition.setFlags(flags));
            return this;
        }

        @Override
        public SetNext<FlowState.Main> set(BiConsumer<CaseData, Map<String, Boolean>> flags) {
            checkNull(flags, STATE);
            stateFlowContext.getCurrentTransition()
                .ifPresent(currentTransition -> currentTransition.setDynamicFlags(flags));
            return this;
        }

        @Override
        public StateNext<FlowState.Main> state(FlowState.Main state) {
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

        private String fullyQualified(FlowState.Main state) {
            return String.format("%s.%s", flowName, state.toString());
        }
    }
}
