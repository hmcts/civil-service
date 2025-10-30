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

class SdoNotDrawnContributorTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow stateFlow;

    private SdoNotDrawnContributor contributor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        contributor = new SdoNotDrawnContributor(
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
    void supportsReturnsFalseWhenMissingData() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThat(contributor.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsFalseWhenStateNotPresent() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.CLAIM_ISSUED.fullName())));

        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawn(MultiPartyScenario.ONE_V_ONE)
            .build();

        assertThat(contributor.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenAllFieldsPresent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawn(MultiPartyScenario.ONE_V_ONE)
            .build();

        assertThat(contributor.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsSdoNotDrawnEvent() {
        CaseData base = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawn(MultiPartyScenario.ONE_V_ONE)
            .build();
        CaseData caseData = base.toBuilder()
            .reasonNotSuitableSDO(base.getReasonNotSuitableSDO().toBuilder().input("No SDO drawn").build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(10);
        assertThat(history.getMiscellaneous().get(0).getEventCode())
            .isEqualTo(EventType.MISCELLANEOUS.getCode());
        String expected = new RoboticsEventTextFormatter()
            .caseProceedOffline("Judge / Legal Advisor did not draw a Direction's Order: No SDO drawn");
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText()).isEqualTo(expected);
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isEqualTo(base.getUnsuitableSDODate());
    }

    @Test
    void contributeTruncatesLongMessages() {
        CaseData base = CaseDataBuilder.builder()
            .atStateTakenOfflineSDONotDrawn(MultiPartyScenario.ONE_V_ONE)
            .build();
        String longReason = "x".repeat(600);
        CaseData caseData = base.toBuilder()
            .reasonNotSuitableSDO(base.getReasonNotSuitableSDO().toBuilder().input(longReason).build())
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        String message = builder.build().getMiscellaneous().get(0).getEventDetailsText();
        assertThat(message.length()).isEqualTo(250);
    }
}
