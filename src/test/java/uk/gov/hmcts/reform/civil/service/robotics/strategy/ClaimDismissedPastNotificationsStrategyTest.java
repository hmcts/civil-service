package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ClaimDismissedPastNotificationsStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private RoboticsEventTextFormatter textFormatter;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    @InjectMocks
    private ClaimDismissedPastNotificationsStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(List.of());
        when(sequenceGenerator.nextSequence(any())).thenReturn(40, 41);
        when(textFormatter.claimDismissedNoActionSinceIssue())
            .thenReturn("RPA Reason: Claim dismissed. Claimant hasn't taken action since the claim was issued.");
        when(textFormatter.claimDismissedNoClaimDetailsWithinWindow())
            .thenReturn("RPA Reason: Claim dismissed. Claimant hasn't notified defendant of the claim details within the allowed 2 weeks.");
    }

    @Test
    void supportsReturnsFalseWhenDismissedDateMissing() {
        CaseData caseData = CaseData.builder().build();
        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenNotificationStatePresent() {
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(State.from(FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName()))
        );
        CaseData caseData = CaseData.builder()
            .claimDismissedDate(LocalDateTime.of(2024, 2, 6, 9, 0))
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsEventsForMatchedStatesInOrder() {
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(
                State.from(FlowState.Main.CLAIM_NOTIFIED.fullName()),
                State.from(FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName())
            )
        );
        LocalDateTime dismissedDate = LocalDateTime.of(2024, 2, 6, 9, 0);
        CaseData caseData = CaseData.builder()
            .claimDismissedDate(dismissedDate)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(40);
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isEqualTo(dismissedDate);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("RPA Reason: Claim dismissed. Claimant hasn't notified defendant of the claim details within the allowed 2 weeks.");
    }

    @Test
    void contributeHandlesMultipleMatchedStates() {
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(
                State.from(FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName()),
                State.from(FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName())
            )
        );
        LocalDateTime dismissedDate = LocalDateTime.of(2024, 2, 6, 9, 0);
        CaseData caseData = CaseData.builder()
            .claimDismissedDate(dismissedDate)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(2);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(40);
        assertThat(history.getMiscellaneous().get(1).getEventSequence()).isEqualTo(41);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("RPA Reason: Claim dismissed. Claimant hasn't taken action since the claim was issued.");
        assertThat(history.getMiscellaneous().get(1).getEventDetailsText())
            .isEqualTo("RPA Reason: Claim dismissed. Claimant hasn't notified defendant of the claim details within the allowed 2 weeks.");
    }

    @Test
    void supportsReturnsFalseWhenNoRelevantState() {
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(State.from(FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()))
        );

        CaseData caseData = CaseData.builder()
            .claimDismissedDate(LocalDateTime.of(2024, 2, 6, 9, 0))
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void contributeUsesNextSequenceForMultipleEvents() {
        when(sequenceGenerator.nextSequence(any())).thenReturn(100, 101, 102);
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(
                State.from(FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName()),
                State.from(FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName())
            )
        );
        CaseData caseData = CaseData.builder()
            .claimDismissedDate(LocalDateTime.of(2024, 3, 3, 10, 0))
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.build().getMiscellaneous())
            .extracting(Event::getEventSequence)
            .containsExactly(100, 101);
    }
}
