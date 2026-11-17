package uk.gov.hmcts.reform.civil.stateflow;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.stateflow.StateFlowContext.EXTENDED_STATE_CASE_KEY;
import static uk.gov.hmcts.reform.civil.stateflow.StateFlowContext.EXTENDED_STATE_ERROR_KEY;

class StateFlowListenerTest {

    @SuppressWarnings("unchecked")
    @Test
    void shouldLogCaseIdAndStoreAmbiguity_whenMultipleTransitionsArePermitted() {
        StateMachine<String, String> stateMachine = mock(StateMachine.class);
        StateContext<String, String> stateContext = mock(StateContext.class);
        ExtendedState extendedState = mock(ExtendedState.class);
        Map<Object, Object> variables = new HashMap<>();
        CaseData caseData = CaseData.builder().ccdCaseReference(1234567890L).build();
        State<String, String> sourceState = createState("SOURCE");
        List<Transition<String, String>> transitions = List.of(
            createTransition(sourceState, createState("TARGET_A")),
            createTransition(sourceState, createState("TARGET_B"))
        );
        when(stateMachine.getExtendedState()).thenReturn(extendedState);
        when(stateMachine.getTransitions()).thenReturn(transitions);
        when(stateContext.getStateMachine()).thenReturn(stateMachine);
        when(extendedState.get(EXTENDED_STATE_CASE_KEY, CaseData.class)).thenReturn(caseData);
        when(extendedState.getVariables()).thenReturn(variables);

        StateFlowListener listener = new StateFlowListener();
        listener.stateContext(stateContext);

        Logger logger = (Logger) LoggerFactory.getLogger(StateFlowListener.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            listener.stateEntered(sourceState);

            String ambiguityMessage = "Ambiguous transitions permitting state [SOURCE] to move to more than "
                + "one next states [TARGET_A,TARGET_B].";
            assertThat(variables).containsEntry(EXTENDED_STATE_ERROR_KEY, ambiguityMessage);
            assertThat(appender.list)
                .singleElement()
                .extracting(ILoggingEvent::getFormattedMessage)
                .isEqualTo(ambiguityMessage + " Case ID: 1234567890");
            verify(stateMachine).setStateMachineError(org.mockito.ArgumentMatchers.argThat(
                error -> error.getMessage().equals(ambiguityMessage)
            ));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @SuppressWarnings("unchecked")
    private State<String, String> createState(String stateId) {
        State<String, String> state = mock(State.class);
        when(state.getId()).thenReturn(stateId);
        return state;
    }

    @SuppressWarnings("unchecked")
    private Transition<String, String> createTransition(State<String, String> source, State<String, String> target) {
        Transition<String, String> transition = mock(Transition.class);
        when(transition.getSource()).thenReturn(source);
        when(transition.getTarget()).thenReturn(target);
        when(transition.getGuard()).thenReturn(null);
        return transition;
    }
}