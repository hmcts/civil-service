package uk.gov.hmcts.reform.civil.stateflow;

import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StateFlowContext {

    protected static final String EXTENDED_STATE_CASE_KEY = "case";
    protected static final String EXTENDED_STATE_HISTORY_KEY = "history";

    private final List<String> states = new ArrayList<>();

    private final List<Transition> transitions = new ArrayList<>();

    Optional<String> getInitialState() {
        return states.isEmpty() ? Optional.empty() : Optional.of(states.get(0));
    }

    Optional<String> getCurrentState() {
        return states.isEmpty() ? Optional.empty() : Optional.of(states.get(states.size() - 1));
    }

    Optional<Transition> getCurrentTransition() {
        return transitions.isEmpty() ? Optional.empty() : Optional.of(transitions.get(transitions.size() - 1));
    }

    public List<String> addState(String state) {
        this.states.add(state);
        return this.states;
    }

    public List<Transition> addTransition(Transition transition) {
        this.transitions.add(transition);
        return this.transitions;
    }

    public List<String> getStates() {
        return states;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }
}
