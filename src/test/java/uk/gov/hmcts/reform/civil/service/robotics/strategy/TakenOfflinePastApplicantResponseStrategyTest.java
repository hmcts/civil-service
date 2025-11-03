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
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TakenOfflinePastApplicantResponseStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private RoboticsEventTextFormatter textFormatter;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    @InjectMocks
    private TakenOfflinePastApplicantResponseStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(State.from(FlowState.Main.TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE.fullName()))
        );
        when(textFormatter.claimMovedOfflineAfterApplicantResponseDeadline())
            .thenReturn("RPA Reason: Claim moved offline after no response from applicant past response deadline.");
    }

    @Test
    void supportsReturnsFalseWhenTakenOfflineDateMissing() {
        assertThat(strategy.supports(CaseData.builder().build())).isFalse();
    }

    @Test
    void supportsReturnsFalseWhenStateMissing() {
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(State.from(FlowState.Main.CLAIM_NOTIFIED.fullName()))
        );
        CaseData caseData = CaseData.builder()
            .takenOfflineDate(LocalDateTime.now())
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenStatePresent() {
        CaseData caseData = CaseData.builder()
            .takenOfflineDate(LocalDateTime.of(2024, 2, 5, 12, 0))
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsMiscEvent() {
        LocalDateTime takenOffline = LocalDateTime.of(2024, 2, 5, 12, 0);
        CaseData caseData = CaseData.builder()
            .takenOfflineDate(takenOffline)
            .build();
        when(sequenceGenerator.nextSequence(any())).thenReturn(34);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(34);
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isEqualTo(takenOffline);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("RPA Reason: Claim moved offline after no response from applicant past response deadline.");
    }
}
