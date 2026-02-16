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
import static org.mockito.Mockito.verifyNoInteractions;
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
    void supportsReturnsFalseWhenCaseDataNull() {
        assertThat(strategy.supports(null)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenStatePresentAndResponseExists() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentPartAdmission()
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeDoesNothingWhenNotSupported() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.CLAIM_ISSUED.fullName())));

        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentPartAdmission()
            .build();

        EventHistory historyBuilder = new EventHistory();
        strategy.contribute(historyBuilder, caseData, null);

        assertThat(historyBuilder.getReceiptOfPartAdmission()).isNullOrEmpty();
        assertThat(historyBuilder.getMiscellaneous()).isNullOrEmpty();
        assertThat(historyBuilder.getDirectionsQuestionnaireFiled()).isNullOrEmpty();
        verifyNoInteractions(sequenceGenerator);
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

        EventHistory historyBuilder = new EventHistory();
        strategy.contribute(historyBuilder, caseData, null);

        assertThat(historyBuilder.getReceiptOfPartAdmission()).hasSize(1);
        assertThat(historyBuilder.getReceiptOfPartAdmission().getFirst().getEventSequence()).isEqualTo(10);
        assertThat(historyBuilder.getReceiptOfPartAdmission().getFirst().getDateReceived())
            .isEqualTo(caseData.getRespondent1ResponseDate());

        assertThat(historyBuilder.getMiscellaneous()).hasSize(1);
        assertThat(historyBuilder.getMiscellaneous().getFirst().getEventSequence()).isEqualTo(11);
        assertThat(historyBuilder.getMiscellaneous().getFirst().getEventDetailsText()).isEqualTo(
            respondentResponseSupport.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true)
        );

        assertThat(historyBuilder.getDirectionsQuestionnaireFiled()).hasSize(1);
        assertThat(historyBuilder.getDirectionsQuestionnaireFiled().getFirst().getEventSequence()).isEqualTo(12);
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

        EventHistory historyBuilder = new EventHistory();
        strategy.contribute(historyBuilder, caseData, null);

        assertThat(historyBuilder.getReceiptOfPartAdmission())
            .extracting(Event::getEventSequence)
            .containsExactly(10, 13);

        assertThat(historyBuilder.getMiscellaneous())
            .extracting(Event::getEventSequence)
            .containsExactly(11, 14);

        assertThat(historyBuilder.getDirectionsQuestionnaireFiled())
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

        EventHistory historyBuilder = new EventHistory();
        strategy.contribute(historyBuilder, caseData, null);

        assertThat(historyBuilder.getReceiptOfPartAdmission())
            .extracting(Event::getEventSequence)
            .containsExactly(10, 13);

        assertThat(historyBuilder.getMiscellaneous())
            .extracting(Event::getEventSequence)
            .containsExactly(11, 14);

        assertThat(historyBuilder.getDirectionsQuestionnaireFiled())
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

        EventHistory historyBuilder = new EventHistory();
        LocalDateTime before = LocalDateTime.now();
        strategy.contribute(historyBuilder, caseData, null);
        LocalDateTime after = LocalDateTime.now();

        assertThat(historyBuilder.getMiscellaneous().getFirst().getDateReceived()).isAfterOrEqualTo(before);
        assertThat(historyBuilder.getMiscellaneous().getFirst().getDateReceived()).isBeforeOrEqualTo(after);
        assertThat(historyBuilder.getMiscellaneous()).hasSize(2);
        assertThat(historyBuilder.getMiscellaneous().getFirst().getEventSequence()).isEqualTo(10);
        assertThat(historyBuilder.getMiscellaneous().getFirst().getEventDetailsText())
            .isEqualTo(formatter.lipVsLrFullOrPartAdmissionReceived());

        assertThat(historyBuilder.getMiscellaneous().get(1).getEventDetailsText())
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

        EventHistory historyBuilder = new EventHistory();
        strategy.contribute(historyBuilder, caseData, null);

        assertThat(historyBuilder.getReceiptOfPartAdmission()).hasSize(2);
        assertThat(historyBuilder.getReceiptOfPartAdmission())
            .extracting(Event::getDateReceived)
            .containsOnly(caseData.getRespondent1ResponseDate());
        assertThat(historyBuilder.getMiscellaneous()).hasSize(2);
        assertThat(historyBuilder.getDirectionsQuestionnaireFiled()).hasSize(2);
    }

    @Test
    void contributeAddsStatesPaidWhenSpecRequiresAdmission() {
        CaseDataBuilder builder = CaseDataBuilder.builder().setClaimTypeToSpecClaim();
        builder.respondent1DQ();
        CaseData caseData = builder
            .atStateRespondentPartAdmissionSpec()
            .applicant1Represented(YES)
            .build();
        caseData.setSpecDefenceAdmittedRequired(YES);

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11);

        EventHistory historyBuilder = new EventHistory();
        strategy.contribute(historyBuilder, caseData, null);

        assertThat(historyBuilder.getStatesPaid()).hasSize(1);
        assertThat(historyBuilder.getStatesPaid().getFirst().getEventSequence()).isEqualTo(10);
        assertThat(historyBuilder.getStatesPaid().getFirst().getEventCode()).isEqualTo(EventType.STATES_PAID.getCode());

        assertThat(historyBuilder.getReceiptOfPartAdmission()).isNullOrEmpty();
        assertThat(historyBuilder.getMiscellaneous()).isNullOrEmpty();
    }

    @Test
    void contributeAddsReceiptOfPartAdmissionWhenSpecDoesNotRequireStatesPaid() {
        CaseDataBuilder builder = CaseDataBuilder.builder().setClaimTypeToSpecClaim();
        builder.respondent1DQ();
        CaseData caseData = builder
            .atStateRespondentPartAdmissionSpec()
            .applicant1Represented(YES)
            .build();
        caseData.setSpecDefenceAdmittedRequired(NO);

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(20, 21);

        EventHistory historyBuilder = new EventHistory();
        strategy.contribute(historyBuilder, caseData, null);

        assertThat(historyBuilder.getReceiptOfPartAdmission()).hasSize(1);
        assertThat(historyBuilder.getReceiptOfPartAdmission().getFirst().getEventSequence()).isEqualTo(20);
        assertThat(historyBuilder.getStatesPaid()).isNullOrEmpty();
        assertThat(historyBuilder.getMiscellaneous()).isNullOrEmpty();
    }
}
