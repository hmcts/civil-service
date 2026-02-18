package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonNotSuitableSDO;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SdoNotDrawnStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow stateFlow;

    private SdoNotDrawnStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        strategy = new SdoNotDrawnStrategy(
            sequenceGenerator,
            new RoboticsEventTextFormatter(),
            stateFlowEngine
        );

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(List.of(
            State.from(FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN.fullName())
        ));
    }

    @Test
    void supportsReturnsTrueWhenStatePresentEvenIfDetailsMissing() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void supportsReturnsFalseWhenStateNotPresent() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.CLAIM_ISSUED.fullName())));

        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawn(MultiPartyScenario.ONE_V_ONE)
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenAllFieldsPresent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawn(MultiPartyScenario.ONE_V_ONE)
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsSdoNotDrawnEvent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawn(MultiPartyScenario.ONE_V_ONE)
            .reasonNotSuitableSDO(new ReasonNotSuitableSDO("No SDO drawn"))
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getMiscellaneous()).hasSize(1);
        assertThat(builder.getMiscellaneous().getFirst().getEventSequence()).isEqualTo(10);
        assertThat(builder.getMiscellaneous().getFirst().getEventCode())
            .isEqualTo(EventType.MISCELLANEOUS.getCode());
        String expected = new RoboticsEventTextFormatter()
            .caseProceedOffline("Judge / Legal Advisor did not draw a Direction's Order: No SDO drawn");
        assertThat(builder.getMiscellaneous().getFirst().getEventDetailsText()).isEqualTo(expected);
        assertThat(builder.getMiscellaneous().getFirst().getDateReceived()).isEqualTo(caseData.getUnsuitableSDODate());
    }

    @Test
    void contributeTruncatesLongMessages() {
        String longReason = "x".repeat(600);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawn(MultiPartyScenario.ONE_V_ONE)
            .reasonNotSuitableSDO(new ReasonNotSuitableSDO(longReason))
            .build();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        String message = builder.getMiscellaneous().getFirst().getEventDetailsText();
        assertThat(message).hasSize(250);
    }
}
