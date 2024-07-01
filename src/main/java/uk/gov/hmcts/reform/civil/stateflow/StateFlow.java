package uk.gov.hmcts.reform.civil.stateflow;

import org.springframework.statemachine.StateMachine;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.stateflow.exception.StateFlowException;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.stateflow.StateFlowContext.EXTENDED_STATE_CASE_KEY;
import static uk.gov.hmcts.reform.civil.stateflow.StateFlowContext.EXTENDED_STATE_FLAGS_KEY;
import static uk.gov.hmcts.reform.civil.stateflow.StateFlowContext.EXTENDED_STATE_HISTORY_KEY;

public class StateFlow {

    private StateMachine<String, String> stateMachine;

    public StateFlow(StateMachine<String, String> stateMachine) {
        this.stateMachine = stateMachine;
    }

    public StateMachine<String, String> asStateMachine() {
        return stateMachine;
    }

    public StateFlow evaluate(CaseData caseData) {
        Map<Object, Object> variables = stateMachine.getExtendedState().getVariables();
        variables.put(EXTENDED_STATE_CASE_KEY, caseData);
        variables.put(EXTENDED_STATE_FLAGS_KEY, new HashMap<String, Boolean>());
        stateMachine.startReactively().block();
        return this;
    }

    public State getState() {
        if (stateMachine.hasStateMachineError()) {
            throw new StateFlowException("The state machine is at error state.");
        }
        return State.from(stateMachine.getState().getId());
    }

    @SuppressWarnings("unchecked")
    public List<State> getStateHistory() {
        List<String> historyList = stateMachine.getExtendedState().get(EXTENDED_STATE_HISTORY_KEY, ArrayList.class);
        return historyList.stream().map(State::from).toList();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Boolean> getFlags() {
        return stateMachine.getExtendedState().get(EXTENDED_STATE_FLAGS_KEY, Map.class);
    }

    public boolean isFlagSet(FlowFlag flowFlag) {
        return Optional.ofNullable(getFlags().get(flowFlag.name())).orElse(false);
    }
}
