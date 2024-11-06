package uk.gov.hmcts.reform.civil.stateflow.simplegrammar;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
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
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;
import uk.gov.hmcts.reform.civil.stateflow.transitions.MidTransitionBuilder;
import uk.gov.hmcts.reform.civil.stateflow.transitions.SpecDraftTransitionBuilder;
import uk.gov.hmcts.reform.civil.stateflow.transitions.TransitionBuilder;
import uk.gov.hmcts.reform.civil.stateflow.transitions.UnspecifiedDraftTransitionBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.stateflow.StateFlowContext.EXTENDED_STATE_CASE_KEY;
import static uk.gov.hmcts.reform.civil.stateflow.StateFlowContext.EXTENDED_STATE_FLAGS_KEY;

/**
 * DSL for creating a StateFlow which wraps a state engine backed by Spring State Machine.
 * Once created a StateFlow can:
 * - evaluate the current state of a Case
 * - return the internal state engine for further processing
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@AllArgsConstructor
public class SimpleStateFlowBuilder {

    private static final String STATE = "state";
    // The internal stateFlowContext object. Methods in the DSL work on this
    private final String flowName = "MAIN";

    private final UnspecifiedDraftTransitionBuilder unspecifiedDraftTransitionBuilder;
    private final SpecDraftTransitionBuilder specDraftTransitionBuilder;
    private final List<MidTransitionBuilder> transitionBuilders;

    public static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    private static void checkNull(Object object, String name) {
        if (object == null) {
            throw new IllegalArgumentException(name + " may not be null");
        }
    }

    @SuppressWarnings("unchecked")
    public StateFlow build(FlowState.Main initialState) {
        StateMachineBuilder.Builder<String, String> stateMachineBuilder =
            StateMachineBuilder.builder();
        StateFlowContext stateFlowContext = new StateFlowContext();
        try {
            // Config
            stateMachineBuilder.configureConfiguration()
                .withConfiguration()
                .autoStartup(false);
            // States
            buildStatesOnContext(initialState, stateFlowContext);
            StateConfigurer<String, String> statesConfigurer =
                stateMachineBuilder.configureStates().withStates();
            stateFlowContext.getInitialState().ifPresent(statesConfigurer::initial);
            stateFlowContext.getStates().forEach(statesConfigurer::state);
            // Transitions
            buildTransitionsForContext(stateFlowContext, initialState);

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

    private void buildStatesOnContext(FlowState.Main initialState, StateFlowContext stateFlowContext) {
        FlowState.Main[] states = FlowState.Main.values();
        List<FlowState.Main> statesList = Arrays.asList(states);
        if (initialState.equals(FlowState.Main.DRAFT)) {
            statesList = new ArrayList<>(statesList);
            statesList.remove(FlowState.Main.SPEC_DRAFT);
            states = statesList.toArray(new FlowState.Main[0]);
        } else {
            statesList = new ArrayList<>(statesList);
            statesList.remove(FlowState.Main.DRAFT);
            states = statesList.toArray(new FlowState.Main[0]);
        }
        for (FlowState.Main state : states) {
            addState(state, stateFlowContext);
        }
    }

    private void buildTransitionsForContext(StateFlowContext stateFlowContext, FlowState.Main initialState) {
        List<TransitionBuilder> allTransitionBuilders = new ArrayList<>();
        if (initialState.equals(FlowState.Main.DRAFT)) {
            allTransitionBuilders.add(unspecifiedDraftTransitionBuilder);
            allTransitionBuilders.addAll(transitionBuilders);
        } else {
            allTransitionBuilders.add(specDraftTransitionBuilder);
            allTransitionBuilders.addAll(transitionBuilders);
        }
        List<Transition> transitions = new ArrayList<>();
        allTransitionBuilders.forEach(transitionBuilder -> {
            transitions.addAll(transitionBuilder.buildTransitions());
        });
        ListIterator<Transition> iterator = transitions.listIterator();
        while (iterator.hasNext()) {
            stateFlowContext.addTransition(iterator.next());
        }
    }

    private void addState(FlowState.Main state, StateFlowContext stateFlowContext) {
        checkNull(state, STATE);
        stateFlowContext.addState(fullyQualified(state));
    }

    private String fullyQualified(FlowState.Main state) {
        return String.format("%s.%s", flowName, state.toString());
    }

}
