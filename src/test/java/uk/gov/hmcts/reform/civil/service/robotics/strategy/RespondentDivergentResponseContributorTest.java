package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
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

class RespondentDivergentResponseContributorTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2024, 6, 20, 9, 15);

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow stateFlow;

    private RoboticsRespondentResponseSupport respondentResponseSupport;
    private RespondentDivergentResponseContributor contributor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RoboticsEventTextFormatter formatter = new RoboticsEventTextFormatter();
        RoboticsTimelineHelper timelineHelper = new RoboticsTimelineHelper(() -> NOW);
        respondentResponseSupport = new RoboticsRespondentResponseSupport(formatter, timelineHelper);
        contributor = new RespondentDivergentResponseContributor(
            sequenceGenerator,
            respondentResponseSupport,
            stateFlowEngine
        );

        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
    }

    @Test
    void supportsReturnsFalseWhenStateMissing() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.CLAIM_ISSUED.fullName())));

        CaseData caseData = createUnspecDivergentCase();

        assertThat(contributor.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsFalseWhenNoResponses() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(
            State.from(FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED.fullName())
        ));

        CaseData caseData = CaseDataBuilder.builder().build();

        assertThat(contributor.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenStatePresentAndResponsesExist() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(
            State.from(FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName())
        ));

        CaseData caseData = createUnspecDivergentCase();

        assertThat(contributor.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsEventsForUnspecDivergentResponses() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(
            State.from(FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED.fullName())
        ));
        when(stateFlow.getState()).thenReturn(
            State.from(FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED.fullName())
        );
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13);

        CaseData caseData = createUnspecDivergentCase();

        assertThat(contributor.supports(caseData)).isTrue();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();

        assertThat(history.getDefenceFiled())
            .extracting(Event::getEventSequence)
            .containsExactly(10);

        assertThat(history.getDirectionsQuestionnaireFiled())
            .extracting(Event::getEventSequence)
            .containsExactly(11);

        assertThat(history.getReceiptOfPartAdmission())
            .extracting(Event::getEventSequence)
            .containsExactly(12);

        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventSequence)
            .containsExactly(13);

        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventDetails)
            .extracting(EventDetails::getMiscText)
            .containsExactly(
                respondentResponseSupport.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false)
            );
    }

    @Test
    void contributeAddsSpecMiscOnlyWhenOffline() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(
            State.from(FlowState.Main.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED.fullName()),
            State.from(FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE.fullName())
        ));
        when(stateFlow.getState()).thenReturn(
            State.from(FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE.fullName())
        );
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(20, 21, 22, 23);

        CaseData caseData = createSpecDivergentCase();

        assertThat(contributor.supports(caseData)).isTrue();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        contributor.contribute(builder, caseData, null);

        EventHistory history = builder.build();

        assertThat(history.getReceiptOfPartAdmission())
            .extracting(Event::getEventSequence)
            .containsExactly(20);

        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventSequence)
            .containsExactly(21);

        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventDetails)
            .extracting(EventDetails::getMiscText)
            .containsExactly(
                respondentResponseSupport.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true)
            );

        assertThat(history.getDefenceFiled())
            .extracting(Event::getEventSequence)
            .containsExactly(22);

        assertThat(history.getDirectionsQuestionnaireFiled())
            .extracting(Event::getEventSequence)
            .containsExactly(23);
    }

    private CaseData createUnspecDivergentCase() {
        CaseDataBuilder builder = CaseDataBuilder.builder();
        builder.respondent1DQ();
        builder.respondent2DQ();
        builder.multiPartyClaimTwoDefendantSolicitors();
        builder.respondent1(PartyBuilder.builder().individual().build());
        builder.respondent2(PartyBuilder.builder().individual().build());
        return builder
            .respondent1ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
            .respondent2ClaimResponseType(RespondentResponseType.PART_ADMISSION)
            .respondent1ResponseDate(NOW)
            .respondent2ResponseDate(NOW.plusDays(1))
            .build();
    }

    private CaseData createSpecDivergentCase() {
        CaseDataBuilder builder = CaseDataBuilder.builder();
        builder.respondent1DQ();
        builder.respondent2DQ();
        builder.setClaimTypeToSpecClaim();
        builder.multiPartyClaimTwoDefendantSolicitorsSpec();
        Party respondent1 = PartyBuilder.builder().individual().build();
        Party respondent2 = PartyBuilder.builder().individual().build();
        builder.respondent1(respondent1);
        builder.respondent2(respondent2);
        return builder
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent1ResponseDate(NOW)
            .respondent2ResponseDate(NOW.plusDays(2))
            .build();
    }
}
