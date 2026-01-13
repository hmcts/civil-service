package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UnrepresentedDefendantStrategyTest {

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Mock
    private RoboticsTimelineHelper timelineHelper;
    @Mock
    private RoboticsEventTextFormatter textFormatter;
    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;

    @InjectMocks
    private UnrepresentedDefendantStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(
            List.of(State.from(FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT.fullName()))
        );
        when(timelineHelper.now()).thenReturn(LocalDateTime.of(2024, 2, 12, 9, 0));
        when(sequenceGenerator.nextSequence(any())).thenReturn(11, 12);
        when(textFormatter.unrepresentedDefendant("[1 of 2 - 2024-02-12] ", "Resp One"))
            .thenReturn("RPA Reason: [1 of 2 - 2024-02-12] Unrepresented defendant: Resp One");
        when(textFormatter.unrepresentedDefendant("[2 of 2 - 2024-02-12] ", "Resp Two"))
            .thenReturn("RPA Reason: [2 of 2 - 2024-02-12] Unrepresented defendant: Resp Two");
        when(textFormatter.unrepresentedDefendant("", "Resp One"))
            .thenReturn("RPA Reason: Unrepresented defendant: Resp One");
    }

    @Test
    void supportsReturnsTrueWhenStatePresentEvenIfSubmittedDateMissing() {
        assertThat(strategy.supports(CaseData.builder().build())).isTrue();
    }

    @Test
    void supportsReturnsTrueWhenStatePresent() {
        CaseData caseData = CaseData.builder()
            .submittedDate(LocalDateTime.now())
            .respondent1(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Resp")
                .individualLastName("One")
                .build())
            .respondent1Represented(YesOrNo.NO)
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsEventsForEachUnrepresentedDefendant() {
        CaseData caseData = CaseData.builder()
            .submittedDate(LocalDateTime.of(2024, 2, 10, 0, 0))
            .respondent1(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Resp")
                .individualLastName("One")
                .build())
            .respondent1Represented(YesOrNo.NO)
            .respondent2(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Resp")
                .individualLastName("Two")
                .build())
            .respondent2Represented(YesOrNo.NO)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(2);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(11);
        assertThat(history.getMiscellaneous().get(0).getDateReceived())
            .isEqualTo(LocalDateTime.of(2024, 2, 10, 0, 0));
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo("RPA Reason: [1 of 2 - 2024-02-12] Unrepresented defendant: Resp One");
        assertThat(history.getMiscellaneous().get(1).getEventSequence()).isEqualTo(12);
        assertThat(history.getMiscellaneous().get(1).getEventDetailsText())
            .isEqualTo("RPA Reason: [2 of 2 - 2024-02-12] Unrepresented defendant: Resp Two");
    }

    @Test
    void supportsReturnsTrueWhenStatePresentEvenIfNoUnrepresentedDefendants() {
        CaseData caseData = CaseData.builder()
            .submittedDate(LocalDateTime.now())
            .respondent1(Party.builder().individualFirstName("Resp").individualLastName("One")
                .type(Party.Type.INDIVIDUAL).build())
            .respondent1Represented(YesOrNo.YES)
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsSingleEventWhenOnlyFirstUnrepresented() {
        CaseData caseData = CaseData.builder()
            .submittedDate(LocalDateTime.of(2024, 2, 10, 0, 0))
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL)
                .individualFirstName("Resp").individualLastName("One").build())
            .respondent1Represented(YesOrNo.NO)
            .respondent2(Party.builder().type(Party.Type.INDIVIDUAL)
                .individualFirstName("Resp").individualLastName("Two").build())
            .respondent2Represented(YesOrNo.YES)
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText()).isNotNull();
    }
}
