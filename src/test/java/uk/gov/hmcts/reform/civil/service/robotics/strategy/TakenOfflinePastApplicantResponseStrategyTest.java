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
    void supportsReturnsTrueWhenTakenOfflineDateMissingButStatePresent() {
        assertThat(strategy.supports(CaseDataBuilder.builder().build())).isTrue();
    }

    @Test
    void supportsReturnsFalseWhenStateMissing() {
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(State.from(FlowState.Main.CLAIM_NOTIFIED.fullName()))
        );
        CaseData caseData = CaseDataBuilder.builder()
            .takenOfflineDate(LocalDateTime.now())
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenStatePresent() {
        CaseData caseData = CaseDataBuilder.builder()
            .takenOfflineDate(LocalDateTime.of(2024, 2, 5, 12, 0))
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsMiscEvent() {
        LocalDateTime takenOffline = LocalDateTime.of(2024, 2, 5, 12, 0);
        CaseData caseData = CaseDataBuilder.builder()
            .takenOfflineDate(takenOffline)
            .build();
        when(sequenceGenerator.nextSequence(any())).thenReturn(34);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getMiscellaneous()).hasSize(1);
        assertThat(builder.getMiscellaneous().getFirst().getEventSequence()).isEqualTo(34);
        assertThat(builder.getMiscellaneous().getFirst().getDateReceived()).isEqualTo(takenOffline);
        assertThat(builder.getMiscellaneous().getFirst().getEventDetailsText())
            .isEqualTo("RPA Reason: Claim moved offline after no response from applicant past response deadline.");
    }
}
