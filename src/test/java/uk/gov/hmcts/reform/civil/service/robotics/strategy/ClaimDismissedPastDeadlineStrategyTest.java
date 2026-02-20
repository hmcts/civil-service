package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ClaimDismissedPastDeadlineStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private RoboticsEventTextFormatter textFormatter;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    @InjectMocks
    private ClaimDismissedPastDeadlineStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(sequenceGenerator.nextSequence(any())).thenReturn(50);
        when(textFormatter.claimDismissedAfterNoDefendantResponse())
            .thenReturn("RPA Reason: Claim dismissed after no response from defendant after claimant sent notification.");
        when(textFormatter.claimDismissedNoUserActionForSixMonths())
            .thenReturn("RPA Reason: Claim dismissed. No user action has been taken for 6 months.");
    }

    @Test
    void supportsReturnsTrueWhenHistoryPresentEvenIfDateMissing() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(
            State.from(FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()),
            State.from(FlowState.Main.CLAIM_NOTIFIED.fullName())
        ));
        assertThat(strategy.supports(CaseDataBuilder.builder().build())).isTrue();
    }

    @Test
    void supportsReturnsFalseWhenHistoryInsufficient() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.CLAIM_NOTIFIED.fullName())));
        CaseData caseData = CaseDataBuilder.builder()
            .claimDismissedDate(LocalDateTime.now())
            .build();
        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void contributeUsesPreviousStateToChooseMessage() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(
            State.from(FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()),
            State.from(FlowState.Main.CLAIM_NOTIFIED.fullName()),
            State.from(FlowState.Main.DRAFT.fullName())
        ));

        LocalDateTime dismissed = LocalDateTime.of(2024, 2, 10, 9, 0);
        CaseData caseData = CaseDataBuilder.builder()
            .claimDismissedDate(dismissed)
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getMiscellaneous()).hasSize(1);
        assertThat(builder.getMiscellaneous().getFirst().getEventSequence()).isEqualTo(50);
        assertThat(builder.getMiscellaneous().getFirst().getDateReceived()).isEqualTo(dismissed);
        assertThat(builder.getMiscellaneous().getFirst().getEventDetailsText())
            .isEqualTo("RPA Reason: Claim dismissed after no response from defendant after claimant sent notification.");
    }

    @Test
    void contributeThrowsWhenPreviousStateIsPastClaimNotificationAwaitingCamunda() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(
            State.from(FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName()),
            State.from(FlowState.Main.PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName()),
            State.from(FlowState.Main.DRAFT.fullName())
        ));

        CaseData caseData = CaseDataBuilder.builder()
            .claimDismissedDate(LocalDateTime.now())
            .build();
        EventHistory builder = new EventHistory();

        assertThatThrownBy(() -> strategy.contribute(builder, caseData, null))
            .isInstanceOf(IllegalStateException.class);
    }
}
