package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
        assertThat(strategy.supports(CaseDataBuilder.builder().build())).isTrue();
    }

    @Test
    void supportsReturnsTrueWhenStatePresent() {
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setIndividualFirstName("Resp");
        respondent1.setIndividualLastName("One");

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(respondent1)
            .respondent1Represented(YesOrNo.NO)
            .build();
        caseData.setSubmittedDate(LocalDateTime.now());

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsEventsForEachUnrepresentedDefendant() {
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setIndividualFirstName("Resp");
        respondent1.setIndividualLastName("One");
        Party respondent2 = new Party();
        respondent2.setType(Party.Type.INDIVIDUAL);
        respondent2.setIndividualFirstName("Resp");
        respondent2.setIndividualLastName("Two");

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(respondent1)
            .respondent1Represented(YesOrNo.NO)
            .respondent2(respondent2)
            .respondent2Represented(YesOrNo.NO)
            .build();
        caseData.setSubmittedDate(LocalDateTime.of(2024, 2, 10, 0, 0));

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getMiscellaneous()).hasSize(2);
        assertThat(builder.getMiscellaneous().getFirst().getEventSequence()).isEqualTo(11);
        assertThat(builder.getMiscellaneous().getFirst().getDateReceived())
            .isEqualTo(LocalDateTime.of(2024, 2, 10, 0, 0));
        assertThat(builder.getMiscellaneous().getFirst().getEventDetailsText())
            .isEqualTo("RPA Reason: [1 of 2 - 2024-02-12] Unrepresented defendant: Resp One");
        assertThat(builder.getMiscellaneous().get(1).getEventSequence()).isEqualTo(12);
        assertThat(builder.getMiscellaneous().get(1).getEventDetailsText())
            .isEqualTo("RPA Reason: [2 of 2 - 2024-02-12] Unrepresented defendant: Resp Two");
    }

    @Test
    void supportsReturnsTrueWhenStatePresentEvenIfNoUnrepresentedDefendants() {
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setIndividualFirstName("Resp");
        respondent1.setIndividualLastName("One");

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(respondent1)
            .respondent1Represented(YesOrNo.YES)
            .build();
        caseData.setSubmittedDate(LocalDateTime.now());

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsSingleEventWhenOnlyFirstUnrepresented() {
        Party respondent1 = new Party();
        respondent1.setType(Party.Type.INDIVIDUAL);
        respondent1.setIndividualFirstName("Resp");
        respondent1.setIndividualLastName("One");
        Party respondent2 = new Party();
        respondent2.setType(Party.Type.INDIVIDUAL);
        respondent2.setIndividualFirstName("Resp");
        respondent2.setIndividualLastName("Two");

        CaseData caseData = CaseDataBuilder.builder()
            .respondent1(respondent1)
            .respondent1Represented(YesOrNo.NO)
            .respondent2(respondent2)
            .respondent2Represented(YesOrNo.YES)
            .build();
        caseData.setSubmittedDate(LocalDateTime.of(2024, 2, 10, 0, 0));

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getMiscellaneous()).hasSize(1);
        assertThat(builder.getMiscellaneous().getFirst().getEventDetailsText()).isNotNull();
    }
}
