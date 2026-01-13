package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.service.robotics.support.StrategyTestDataFactory;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class RespondentPartAdmissionStrategyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2024, 6, 20, 9, 15);

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow stateFlow;

    private RoboticsEventTextFormatter formatter;
    private RoboticsRespondentResponseSupport respondentResponseSupport;
    private RespondentPartAdmissionStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        formatter = new RoboticsEventTextFormatter();
        RoboticsTimelineHelper timelineHelper = new RoboticsTimelineHelper(() -> NOW);
        respondentResponseSupport = new RoboticsRespondentResponseSupport(formatter, timelineHelper);
        strategy = new RespondentPartAdmissionStrategy(
            sequenceGenerator,
            respondentResponseSupport,
                timelineHelper,
            formatter,
            stateFlowEngine
        );

        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(List.of(
            State.from(FlowState.Main.PART_ADMISSION.fullName())
        ));
    }

    @Test
    void supportsReturnsFalseWhenStateMissing() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.CLAIM_ISSUED.fullName())));

        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentPartAdmission()
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenCaseDataPresent() {
        CaseData caseData = CaseDataBuilder.builder().build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void supportsReturnsTrueWhenStatePresentAndResponseExists() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentPartAdmission()
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsEventsForSingleRespondentUnspec() {
        CaseDataBuilder builder = CaseDataBuilder.builder();
        builder.respondent1DQ();
        CaseData caseData = builder
            .atStateRespondentPartAdmission()
            .applicant1Represented(YES)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12);

        EventHistory.EventHistoryBuilder historyBuilder = EventHistory.builder();
        strategy.contribute(historyBuilder, caseData, null);

        EventHistory history = historyBuilder.build();
        assertThat(history.getReceiptOfPartAdmission()).hasSize(1);
        assertThat(history.getReceiptOfPartAdmission().get(0).getEventSequence()).isEqualTo(10);
        assertThat(history.getReceiptOfPartAdmission().get(0).getDateReceived())
            .isEqualTo(caseData.getRespondent1ResponseDate());

        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(11);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText()).isEqualTo(
            respondentResponseSupport.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true)
        );

        assertThat(history.getDirectionsQuestionnaireFiled()).hasSize(1);
        assertThat(history.getDirectionsQuestionnaireFiled().get(0).getEventSequence()).isEqualTo(12);
    }

    @Test
    void contributeAddsEventsForSameSolicitorSameResponse() {
        CaseDataBuilder builder = CaseDataBuilder.builder();
        builder.respondent1DQ();
        CaseData caseData = builder
            .multiPartyClaimOneDefendantSolicitor()
            .atStateBothRespondentsSameResponse1v2SameSolicitor(RespondentResponseType.PART_ADMISSION)
            .respondentResponseIsSame(YES)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13, 14, 15);

        EventHistory.EventHistoryBuilder historyBuilder = EventHistory.builder();
        strategy.contribute(historyBuilder, caseData, null);

        EventHistory history = historyBuilder.build();
        assertThat(history.getReceiptOfPartAdmission())
            .extracting(Event::getEventSequence)
            .containsExactly(10, 13);

        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventSequence)
            .containsExactly(11, 14);

        assertThat(history.getDirectionsQuestionnaireFiled())
            .extracting(Event::getEventSequence)
            .containsExactly(12, 15);
    }

    @Test
    void contributeAddsEventsForBothRespondentsWithDifferentSolicitors() {
        CaseData caseData = StrategyTestDataFactory.unspecTwoDefendantSolicitorsCase()
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateBothRespondentsSameResponse(RespondentResponseType.PART_ADMISSION)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13, 14, 15);

        EventHistory.EventHistoryBuilder historyBuilder = EventHistory.builder();
        strategy.contribute(historyBuilder, caseData, null);

        EventHistory history = historyBuilder.build();
        assertThat(history.getReceiptOfPartAdmission())
            .extracting(Event::getEventSequence)
            .containsExactly(10, 13);

        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventSequence)
            .containsExactly(11, 14);

        assertThat(history.getDirectionsQuestionnaireFiled())
            .extracting(Event::getEventSequence)
            .containsExactly(12, 15);
    }

    @Test
    void contributeAddsLipVsLrMessageWhenApplicable() {
        CaseDataBuilder builder = CaseDataBuilder.builder();
        builder.respondent1DQ();
        CaseData caseData = builder
            .atStateRespondentPartAdmission()
            .applicant1Represented(NO)
            .respondent1Represented(YES)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13);

        EventHistory.EventHistoryBuilder historyBuilder = EventHistory.builder();
        LocalDateTime before = LocalDateTime.now();
        strategy.contribute(historyBuilder, caseData, null);
        LocalDateTime after = LocalDateTime.now();

        EventHistory history = historyBuilder.build();
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isAfterOrEqualTo(before);
        assertThat(history.getMiscellaneous().get(0).getDateReceived()).isBeforeOrEqualTo(after);
        assertThat(history.getMiscellaneous()).hasSize(2);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(10);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo(formatter.lipVsLrFullOrPartAdmissionReceived());

        assertThat(history.getMiscellaneous().get(1).getEventDetailsText())
            .isEqualTo(respondentResponseSupport.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true));
    }

    @Test
    void contributeUsesRespondent1DateWhenSameSolicitorNoRespondent2Date() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateBothRespondentsSameResponse1v2SameSolicitor(RespondentResponseType.PART_ADMISSION)
            .respondentResponseIsSame(YES)
            .respondent2ResponseDate(null)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12);

        EventHistory.EventHistoryBuilder historyBuilder = EventHistory.builder();
        strategy.contribute(historyBuilder, caseData, null);

        EventHistory history = historyBuilder.build();
        assertThat(history.getReceiptOfPartAdmission()).hasSize(2);
        assertThat(history.getReceiptOfPartAdmission())
            .extracting(Event::getDateReceived)
            .containsOnly(caseData.getRespondent1ResponseDate());
        assertThat(history.getMiscellaneous()).hasSize(2);
        assertThat(history.getDirectionsQuestionnaireFiled()).hasSize(2);
    }

    @Test
    void contributeAddsStatesPaidWhenSpecRequiresAdmission() {
        CaseDataBuilder builder = CaseDataBuilder.builder().setClaimTypeToSpecClaim();
        builder.respondent1DQ();
        CaseData caseData = builder
            .atStateRespondentPartAdmissionSpec()
            .applicant1Represented(YES)
            .build()
            .toBuilder()
            .specDefenceAdmittedRequired(YES)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11);

        EventHistory.EventHistoryBuilder historyBuilder = EventHistory.builder();
        strategy.contribute(historyBuilder, caseData, null);

        EventHistory history = historyBuilder.build();
        assertThat(history.getStatesPaid()).hasSize(1);
        assertThat(history.getStatesPaid().get(0).getEventSequence()).isEqualTo(10);
        assertThat(history.getStatesPaid().get(0).getEventCode()).isEqualTo(EventType.STATES_PAID.getCode());

        assertThat(history.getReceiptOfPartAdmission()).isNullOrEmpty();
        assertThat(history.getMiscellaneous()).isNullOrEmpty();
    }
}
