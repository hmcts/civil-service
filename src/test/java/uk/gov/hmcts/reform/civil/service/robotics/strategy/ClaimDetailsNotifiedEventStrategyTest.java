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
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ClaimDetailsNotifiedEventStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    @InjectMocks
    private ClaimDetailsNotifiedEventStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(State.from(FlowState.Main.CLAIM_DETAILS_NOTIFIED.fullName()))
        );
    }

    @Test
    void supportsReturnsTrueWhenStatePresentEvenIfDateMissing() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void supportsReturnsFalseWhenStateMissing() {
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(State.from(FlowState.Main.CLAIM_NOTIFIED.fullName()))
        );
        CaseData caseData = CaseDataBuilder.builder()
            .claimDetailsNotificationDate(LocalDateTime.now())
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenStatePresent() {
        CaseData caseData = CaseDataBuilder.builder()
            .claimDetailsNotificationDate(LocalDateTime.of(2024, 2, 2, 9, 0))
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsMiscEvent() {
        when(sequenceGenerator.nextSequence(any())).thenReturn(18);
        LocalDateTime notified = LocalDateTime.of(2024, 2, 2, 9, 0);
        CaseData caseData = CaseDataBuilder.builder()
            .claimDetailsNotificationDate(notified)
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getMiscellaneous()).hasSize(1);
        assertThat(builder.getMiscellaneous().getFirst().getEventSequence()).isEqualTo(18);
        assertThat(builder.getMiscellaneous().getFirst().getDateReceived()).isEqualTo(notified);
        assertThat(builder.getMiscellaneous().getFirst().getEventDetailsText())
            .isEqualTo("Claim details notified.");
    }
}
