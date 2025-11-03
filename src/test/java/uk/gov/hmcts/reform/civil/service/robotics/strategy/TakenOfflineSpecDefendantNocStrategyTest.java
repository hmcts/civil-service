package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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

class TakenOfflineSpecDefendantNocStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow stateFlow;

    @InjectMocks
    private TakenOfflineSpecDefendantNocStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new TakenOfflineSpecDefendantNocStrategy(
            sequenceGenerator,
            new RoboticsEventTextFormatter(),
            stateFlowEngine
        );

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(4);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(List.of(
            State.from(FlowState.Main.TAKEN_OFFLINE_SPEC_DEFENDANT_NOC.fullName())
        ));
    }

    @Test
    void supportsReturnsFalseWhenNoTakenOfflineDate() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsFalseWhenStateMissing() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(
            State.from(FlowState.Main.CLAIM_ISSUED.fullName())
        ));

        CaseData caseData = CaseDataBuilder.builder()
            .takenOfflineDate(LocalDateTime.now())
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenStatePresent() {
        CaseData caseData = CaseDataBuilder.builder()
            .takenOfflineDate(LocalDateTime.now())
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsNoticeOfChangeEvent() {
        LocalDateTime offlineDate = LocalDateTime.now();
        CaseData caseData = CaseDataBuilder.builder()
            .takenOfflineDate(offlineDate)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(4);
        assertThat(history.getMiscellaneous().get(0).getEventCode())
            .isEqualTo(EventType.MISCELLANEOUS.getCode());
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("RPA Reason: Notice of Change filed.");
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isEqualTo(offlineDate);
    }
}
