package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DIRECTIONS_QUESTIONNAIRE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.STATES_PAID;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_AND_COUNTER_CLAIM;

class RespondentFullDefenceStrategyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2024, 6, 20, 9, 15);

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow stateFlow;

    private RoboticsRespondentResponseSupport respondentResponseSupport;
    private RespondentFullDefenceStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RoboticsEventTextFormatter formatter = new RoboticsEventTextFormatter();
        RoboticsTimelineHelper timelineHelper = new RoboticsTimelineHelper(() -> NOW);
        respondentResponseSupport = new RoboticsRespondentResponseSupport(formatter, timelineHelper);
        strategy = new RespondentFullDefenceStrategy(sequenceGenerator, respondentResponseSupport, stateFlowEngine);

        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.FULL_DEFENCE.fullName())));
    }

    @Test
    void supportsReturnsFalseWhenStateMissing() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.CLAIM_ISSUED.fullName())));
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .build()
            .toBuilder()
            .ccdState(CaseState.CASE_ISSUED)
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsFalseWhenNoResponsesPresent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .build()
            .toBuilder()
            .respondent1ResponseDate(null)
            .respondent2ResponseDate(null)
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWithFullDefenceResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeAddsDefenceAndDqForSingleRespondent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .applicant1Represented(YES)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getDefenceFiled()).hasSize(1);
        assertThat(history.getDefenceFiled().get(0).getEventSequence()).isEqualTo(10);
        assertThat(history.getDefenceFiled().get(0).getEventCode()).isEqualTo(DEFENCE_FILED.getCode());
        assertThat(history.getDefenceFiled().get(0).getDateReceived()).isEqualTo(caseData.getRespondent1ResponseDate());

        assertThat(history.getDirectionsQuestionnaireFiled()).hasSize(1);
        assertThat(history.getDirectionsQuestionnaireFiled().get(0).getEventSequence()).isEqualTo(11);
        assertThat(history.getDirectionsQuestionnaireFiled().get(0).getEventCode())
            .isEqualTo(DIRECTIONS_QUESTIONNAIRE_FILED.getCode());
        assertThat(history.getDirectionsQuestionnaireFiled().get(0).getEventDetailsText())
            .isEqualTo(respondentResponseSupport.prepareFullDefenceEventText(
                caseData.getRespondent1DQ(),
                caseData,
                true,
                caseData.getRespondent1()
            ));
    }

    @Test
    void contributeAddsCounterClaimEventForSpecResponses() {
        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .atStateRespondentFullDefence()
            .build()
            .toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getDefenceFiled()).hasSize(1);
        assertThat(history.getDefenceFiled().get(0).getEventCode()).isEqualTo(DEFENCE_FILED.getCode());

        assertThat(history.getDefenceAndCounterClaim()).hasSize(1);
        assertThat(history.getDefenceAndCounterClaim().get(0).getEventCode())
            .isEqualTo(DEFENCE_AND_COUNTER_CLAIM.getCode());
        assertThat(history.getDefenceAndCounterClaim().get(0).getLitigiousPartyID()).isEqualTo("002");
    }

    @Test
    void contributeAddsStatesPaidWhenFirstRespondentPaysInFull() {
        CaseData baseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .build();

        CaseData caseData = baseData.toBuilder()
            .totalClaimAmount(BigDecimal.valueOf(100))
            .respondToClaim(RespondToClaim.builder().howMuchWasPaid(BigDecimal.valueOf(10_000)).build())
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getStatesPaid()).hasSize(1);
        assertThat(history.getStatesPaid().get(0).getEventSequence()).isEqualTo(10);
        assertThat(history.getStatesPaid().get(0).getEventCode()).isEqualTo(STATES_PAID.getCode());
        assertThat(history.getDefenceFiled()).isNullOrEmpty();
    }

    @Test
    void contributeHandlesSameSolicitorSameResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateBothRespondentsSameResponse1v2SameSolicitor(FULL_DEFENCE)
            .respondentResponseIsSame(YES)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getDefenceFiled()).hasSize(2);
        assertThat(history.getDefenceFiled())
            .extracting(Event::getEventCode)
            .containsOnly(DEFENCE_FILED.getCode());
        assertThat(history.getDefenceFiled().get(0).getEventSequence()).isEqualTo(10);
        assertThat(history.getDefenceFiled().get(1).getEventSequence())
            .isGreaterThan(history.getDefenceFiled().get(0).getEventSequence());

        assertThat(history.getDirectionsQuestionnaireFiled()).hasSize(2);
        assertThat(history.getDirectionsQuestionnaireFiled())
            .extracting(Event::getEventCode)
            .containsOnly(DIRECTIONS_QUESTIONNAIRE_FILED.getCode());
    }

    @Test
    void contributeAddsStatesPaidForSecondRespondentWhenSameSolicitorPaidInFull() {
        CaseData baseCase = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateBothRespondentsSameResponse1v2SameSolicitor(FULL_DEFENCE)
            .respondentResponseIsSame(YES)
            .build();

        CaseData caseData = baseCase.toBuilder()
            .totalClaimAmount(BigDecimal.valueOf(100))
            .respondToClaim(RespondToClaim.builder().howMuchWasPaid(BigDecimal.valueOf(10_000)).build())
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13, 14);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getStatesPaid()).hasSize(2);
        assertThat(history.getStatesPaid())
            .extracting(Event::getEventCode)
            .containsOnly(STATES_PAID.getCode());
        assertThat(history.getStatesPaid().get(0).getEventSequence()).isEqualTo(10);
        assertThat(history.getStatesPaid().get(1).getEventSequence())
            .isGreaterThan(history.getStatesPaid().get(0).getEventSequence());
        assertThat(history.getStatesPaid())
            .extracting(Event::getEventCode)
            .containsOnly(STATES_PAID.getCode());
        assertThat(history.getDefenceFiled()).hasSize(1);
        assertThat(history.getDefenceFiled().get(0).getEventCode()).isEqualTo(DEFENCE_FILED.getCode());
    }

    @Test
    void contributeHandlesDifferentSolicitorsForRespondents() {
        CaseData caseData = StrategyTestDataFactory.unspecTwoDefendantSolicitorsCase()
            .atStateBothRespondentsSameResponse(FULL_DEFENCE)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getDefenceFiled()).hasSize(2);
        assertThat(history.getDefenceFiled())
            .extracting(Event::getEventCode)
            .containsOnly(DEFENCE_FILED.getCode());

        assertThat(history.getDirectionsQuestionnaireFiled()).hasSize(2);
        assertThat(history.getDirectionsQuestionnaireFiled())
            .extracting(Event::getEventCode)
            .containsOnly(DIRECTIONS_QUESTIONNAIRE_FILED.getCode());
    }

    @Test
    void contributeAddsStatesPaidForSecondRespondentWhenDifferentSolicitorsAndPaid() {
        CaseData baseCase = StrategyTestDataFactory.unspecTwoDefendantSolicitorsCase()
            .atStateBothRespondentsSameResponse(FULL_DEFENCE)
            .build();

        CaseData caseData = baseCase.toBuilder()
            .totalClaimAmount(BigDecimal.valueOf(100))
            .respondToClaim2(RespondToClaim.builder().howMuchWasPaid(BigDecimal.valueOf(10_000)).build())
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getStatesPaid()).hasSize(1);
        assertThat(history.getStatesPaid().get(0).getEventCode()).isEqualTo(STATES_PAID.getCode());
        assertThat(history.getDefenceFiled()).hasSize(1);
        assertThat(history.getDefenceFiled().get(0).getEventCode()).isEqualTo(DEFENCE_FILED.getCode());
    }

    @Test
    void contributeRespectsLrVsLipStatesPaidBranch() {
        CaseData baseCase = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .applicant1Represented(YES)
            .respondent1Represented(NO)
            .build();

        CaseData caseData = baseCase.toBuilder()
            .defenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getStatesPaid())
            .extracting(Event::getEventSequence)
            .containsExactly(10);
    }

    @Test
    void contributeRespectsLrVsLipDefenceBranchWhenNotFullyPaid() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .applicant1Represented(YES)
            .respondent1Represented(NO)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getDefenceFiled())
            .extracting(Event::getEventSequence)
            .containsExactly(10);
        assertThat(history.getStatesPaid()).isNullOrEmpty();
    }

    @Test
    void contributeAddsMiscEventForSpecLipFullDefence() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .setClaimTypeToSpecClaim()
            .applicant1Represented(NO)
            .respondent1Represented(NO)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous())
            .isNotEmpty()
            .extracting(Event::getEventDetailsText)
            .anySatisfy(text -> assertThat(text)
                .contains("Defendant:")
                .contains("has responded: FULL_DEFENCE"));
    }
}
