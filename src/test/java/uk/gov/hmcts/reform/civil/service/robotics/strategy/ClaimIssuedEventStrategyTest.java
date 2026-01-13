package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ClaimIssuedEventStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    @InjectMocks
    private ClaimIssuedEventStrategy strategy;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(State.from(FlowState.Main.CLAIM_ISSUED.fullName()))
        );
    }

    @Test
    void supportsReturnsFalseWhenStateNotPresent() {
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(State.from(FlowState.Main.CLAIM_NOTIFIED.fullName()))
        );
        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenStatePresent() {
        CaseData caseData = CaseData.builder().build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsClaimIssuedEvent() {
        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.of(2024, 1, 10))
            .build();
        when(sequenceGenerator.nextSequence(any())).thenReturn(12);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(12);
        assertThat(history.getMiscellaneous().get(0).getDateReceived())
            .isEqualTo(LocalDate.of(2024, 1, 10).atStartOfDay());
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("Claim issued in CCD.");
    }
}
