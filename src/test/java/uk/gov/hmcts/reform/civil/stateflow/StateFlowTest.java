package uk.gov.hmcts.reform.civil.stateflow;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.stateflow.exception.StateFlowException;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.stateflow.StateFlowContext.EXTENDED_STATE_CASE_KEY;
import static uk.gov.hmcts.reform.civil.stateflow.StateFlowContext.EXTENDED_STATE_HISTORY_KEY;

@ExtendWith(SpringExtension.class)
class StateFlowTest {

    private static String TEST_STATE = "TEST_STATE";

    @Mock
    private StateMachine<String, String> mockedStateMachine;

    @SuppressWarnings("unchecked")
    private Map<Object, Object> createMockedVariables() {
        return mock(Map.class);
    }

    private ExtendedState createMockedExtendedState() {
        return mock(ExtendedState.class);
    }

    private ExtendedState createMockedExtendedState(Map<Object, Object> mockedVariables) {
        ExtendedState mockedExtendedState = createMockedExtendedState();
        when(mockedExtendedState.getVariables()).thenReturn(mockedVariables);
        return mockedExtendedState;
    }

    @SuppressWarnings("unchecked")
    private org.springframework.statemachine.state.State<String, String> createMockedState(String stateName) {
        org.springframework.statemachine.state.State state = mock(org.springframework.statemachine.state.State.class);
        when(state.getId()).thenReturn(stateName);
        return state;
    }

    @SuppressWarnings("unchecked")
    private Mono<Void> createMockedMono() {
        return mock(Mono.class);
    }

    @Nested
    class AsStateMachine {

        @Test
        void shouldReturnAsStateMachine() {
            StateFlow stateFlow = new StateFlow(mockedStateMachine);

            assertThat(stateFlow.asStateMachine()).isSameAs(mockedStateMachine);
        }
    }

    @Nested
    class Evaluate {

        @Test
        void shouldEvaluateCaseDetails() {
            Map<Object, Object> mockedVariables = createMockedVariables();
            ExtendedState mockedExtendedState = createMockedExtendedState(mockedVariables);
            Mono<Void> mockedMono = createMockedMono();

            when(mockedStateMachine.getExtendedState()).thenReturn(mockedExtendedState);
            when(mockedStateMachine.startReactively()).thenReturn(mockedMono);

            CaseData caseData = CaseData.builder().build();

            StateFlow stateFlow = new StateFlow(mockedStateMachine);

            assertThat(stateFlow.evaluate(caseData)).isSameAs(stateFlow);
            verify(mockedVariables).put(EXTENDED_STATE_CASE_KEY, caseData);
            verify(mockedMono).block();
        }
    }

    @Nested
    class GetState {

        @Test
        void shouldGetState_whenStateMachineHasNoErrors() {
            org.springframework.statemachine.state.State<String, String> mockedState = createMockedState(TEST_STATE);
            when(mockedStateMachine.hasStateMachineError()).thenReturn(false);
            when(mockedStateMachine.getState()).thenReturn(mockedState);

            StateFlow stateFlow = new StateFlow(mockedStateMachine);

            assertThat(stateFlow.getState())
                .extracting(State::getName)
                .isNotNull()
                .isEqualTo(TEST_STATE);
        }

        @Test
        void shouldThrowStateFlowException_whenStateMachineHasErrors() {
            when(mockedStateMachine.hasStateMachineError()).thenReturn(true);
            StateFlow stateFlow = new StateFlow(mockedStateMachine);

            Exception exception = assertThrows(StateFlowException.class, stateFlow::getState);
            String expectedMessage = "The state machine is at error state.";
            String actualMessage = exception.getMessage();

            assertEquals(expectedMessage, actualMessage);
        }
    }

    @Nested
    class GetStateHistory {

        @Test
        void shouldGetStateHistory() {
            ArrayList<String> stateHistory = new ArrayList<>(Arrays.asList("FLOW.STATE_1", "FLOW.STATE_2"));

            ExtendedState mockedExtendedState = createMockedExtendedState();
            when(mockedStateMachine.getExtendedState()).thenReturn(mockedExtendedState);
            when(mockedExtendedState.get(EXTENDED_STATE_HISTORY_KEY, ArrayList.class)).thenReturn(stateHistory);

            StateFlow stateFlow = new StateFlow(mockedStateMachine);

            assertThat(stateFlow.getStateHistory())
                .hasSize(2)
                .extracting(State::getName)
                .containsExactly("FLOW.STATE_1", "FLOW.STATE_2");
        }
    }
}
