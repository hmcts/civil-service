package uk.gov.hmcts.reform.civil.stateflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import uk.gov.hmcts.reform.civil.stateflow.utils.StateMachineUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.stateflow.StateFlowContext.EXTENDED_STATE_HISTORY_KEY;

@Slf4j
public class StateFlowListener extends StateMachineListenerAdapter<String, String> {

    private StateContext<String, String> stateContext;

    @Override
    public void stateContext(StateContext<String, String> stateContext) {
        this.stateContext = stateContext;
        super.stateContext(stateContext);
    }

    @Override
    public void stateEntered(State<String, String> state) {
        Collection<Transition<String, String>> permittedTransitions =
            StateMachineUtils.findPermittedTransitionsForState(stateContext, state);
        if (permittedTransitions.size() > 1) {
            String sourceState = state.getId();
            String permittedStates = String.join(",", toPermittedStates(permittedTransitions));
            String message = String.format(
                "Ambiguous transitions permitting state [%s] to move to more than one next states [%s].",
                sourceState, permittedStates
            );
            log.error(message);
            stateContext.getStateMachine().setStateMachineError(new IllegalStateException(message));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void stateChanged(State<String, String> from, State<String, String> to) {
        ExtendedState extendedState = stateContext.getStateMachine().getExtendedState();
        List<String> historyList = extendedState.get(EXTENDED_STATE_HISTORY_KEY, ArrayList.class);
        if (historyList == null) {
            Map<Object, Object> variables = extendedState.getVariables();
            variables.put(EXTENDED_STATE_HISTORY_KEY, new ArrayList<>(Arrays.asList(to.getId())));
        } else {
            historyList.add(0, to.getId());
        }
    }

    private List<String> toPermittedStates(Collection<Transition<String, String>> permittedTransitions) {
        return permittedTransitions.stream()
            .map(transition -> transition.getTarget().getId())
            .collect(Collectors.toList());
    }
}
