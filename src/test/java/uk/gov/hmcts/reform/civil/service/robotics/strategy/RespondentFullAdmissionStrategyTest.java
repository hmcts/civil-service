package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class RespondentFullAdmissionStrategyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2024, 6, 20, 9, 15);

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow stateFlow;

    private RoboticsEventTextFormatter formatter;
    private RoboticsTimelineHelper timelineHelper;
    private RoboticsRespondentResponseSupport respondentResponseSupport;
    private RespondentFullAdmissionStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        formatter = new RoboticsEventTextFormatter();
        timelineHelper = new RoboticsTimelineHelper(() -> NOW);
        respondentResponseSupport = new RoboticsRespondentResponseSupport(formatter, timelineHelper);
        strategy = new RespondentFullAdmissionStrategy(
            sequenceGenerator,
            respondentResponseSupport,
            formatter,
            timelineHelper,
            stateFlowEngine
        );

        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(List.of(
            State.from(FlowState.Main.FULL_ADMISSION.fullName())
        ));
    }

    @Test
    void supportsReturnsFalseWhenStateMissing() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(
            State.from(FlowState.Main.CLAIM_ISSUED.fullName())
        ));
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullAdmission()
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsFalseWhenNoResponsesPresent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullAdmission()
            .respondent1ResponseDate(null)
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWithFullAdmissionStateAndResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullAdmission()
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsEventsForSingleRespondentUnspec() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullAdmission()
            .applicant1Represented(YES)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getReceiptOfAdmission()).hasSize(1);
        assertThat(history.getReceiptOfAdmission().get(0).getEventSequence()).isEqualTo(10);
        assertThat(history.getReceiptOfAdmission().get(0).getDateReceived())
            .isEqualTo(caseData.getRespondent1ResponseDate());

        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventSequence()).isEqualTo(11);
        assertThat(history.getMiscellaneous().get(0).getDateReceived())
            .isEqualTo(caseData.getRespondent1ResponseDate());
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .isEqualTo(respondentResponseSupport.prepareRespondentResponseText(
                caseData, caseData.getRespondent1(), true));
    }

    @Test
    void contributeAddsEventsForBothRespondents() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateBothRespondentsSameResponse(RespondentResponseType.FULL_ADMISSION)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getReceiptOfAdmission()).hasSize(2);
        assertThat(history.getReceiptOfAdmission())
            .extracting(Event::getEventSequence)
            .containsExactly(10, 12);

        assertThat(history.getMiscellaneous()).hasSize(2);
        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventSequence)
            .containsExactly(11, 13);
        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventDetailsText)
            .containsExactly(
                respondentResponseSupport.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true),
                respondentResponseSupport.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false)
            );
    }

    @Test
    void contributeAddsEventsForSameSolicitorSameResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateBothRespondentsSameResponse1v2SameSolicitor(RespondentResponseType.FULL_ADMISSION)
            .respondentResponseIsSame(YES)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getReceiptOfAdmission()).hasSize(2);
        assertThat(history.getReceiptOfAdmission())
            .extracting(Event::getEventSequence)
            .containsExactly(10, 12);

        assertThat(history.getMiscellaneous()).hasSize(2);
        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventSequence)
            .containsExactly(11, 13);
    }

    @Test
    void contributeSkipsMiscEventsForSpecClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .atStateSpec1v1ClaimSubmitted()
            .atStateRespondent1v1FullAdmissionSpec()
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getReceiptOfAdmission()).hasSize(1);
        assertThat(history.getReceiptOfAdmission().get(0).getEventSequence()).isEqualTo(10);

        assertThat(history.getMiscellaneous()).isNullOrEmpty();
    }

    @Test
    void contributeAddsLipVsLrMessageWhenApplicable() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullAdmission()
            .applicant1Represented(NO)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(2);
        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventDetailsText)
            .containsExactly(
                respondentResponseSupport.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true),
                formatter.lipVsLrFullOrPartAdmissionReceived()
            );
        assertThat(history.getMiscellaneous().get(1).getDateReceived()).isEqualTo(NOW);
    }
}
