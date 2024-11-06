package uk.gov.hmcts.reform.civil.service.robotics.builders;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonNotSuitableSDO;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.stateflow.StateFlowContext.EXTENDED_STATE_HISTORY_KEY;

@ExtendWith(MockitoExtension.class)
public class PastClaimDismissedDeadlineBuilderTest {

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private StateMachine<String, String> mockedStateMachine;

    @InjectMocks
    private PastClaimDismissedDeadlineBuilder pastClaimDismissedDeadlineBuilder;

    @Test
    public void buildEvent() {
        CaseData caseData = CaseDataBuilder.builder().atStatePastClaimDismissedDeadline()
            .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
            .reasonNotSuitableSDO(ReasonNotSuitableSDO.builder().build())
            .build();

        final StateFlow stateFlow = new StateFlow(mockedStateMachine);

        final EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        final EventHistoryDTO eventHistoryDTO = EventHistoryDTO.builder().builder(builder).caseData(caseData).build();
        final ArrayList<String> stateHistory = new ArrayList<>(Arrays.asList("MAIN.CLAIM_NOTIFIED", "MAIN.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE"));

        final ExtendedState mockedExtendedState = createMockedExtendedState();
        when(mockedStateMachine.getExtendedState()).thenReturn(mockedExtendedState);
        when(mockedExtendedState.get(EXTENDED_STATE_HISTORY_KEY, ArrayList.class)).thenReturn(stateHistory);
        when(stateFlowEngine.evaluate(caseData))
            .thenReturn(stateFlow);

        pastClaimDismissedDeadlineBuilder.buildEvent(eventHistoryDTO);

        final Event expectedEvent = Event.builder()
            .eventSequence(1)
            .eventCode("999")
            .dateReceived(null)
            .eventDetailsText("RPA Reason: Claim dismissed after no response from defendant after claimant sent notification.")
            .eventDetails(EventDetails.builder()
                .miscText("RPA Reason: Claim dismissed after no response from defendant after claimant sent notification.")
                .build())
            .build();

        assertThat(builder.build()).isNotNull();
        assertThat(builder.build())
            .extracting("miscellaneous")
            .asInstanceOf(InstanceOfAssertFactories.list(Event.class))
            .containsExactly(expectedEvent);
    }

    private ExtendedState createMockedExtendedState() {
        return mock(ExtendedState.class);
    }
}
