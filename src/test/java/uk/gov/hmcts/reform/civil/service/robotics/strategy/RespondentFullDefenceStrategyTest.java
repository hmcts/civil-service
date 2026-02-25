package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.service.robotics.support.StrategyTestDataFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DIRECTIONS_QUESTIONNAIRE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.STATES_PAID;

class RespondentFullDefenceStrategyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2024, 6, 20, 9, 15);

    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;

    private RoboticsRespondentResponseSupport respondentResponseSupport;
    private RespondentFullDefenceStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RoboticsEventTextFormatter formatter = new RoboticsEventTextFormatter();
        RoboticsTimelineHelper timelineHelper = new RoboticsTimelineHelper(() -> NOW);
        respondentResponseSupport = new RoboticsRespondentResponseSupport(formatter, timelineHelper);
        strategy = new RespondentFullDefenceStrategy(sequenceGenerator, respondentResponseSupport);
    }

    @Test
    void supportsReturnsTrueWhenCaseDataPresent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .build();
        caseData.setRespondent1ResponseDate(null);
        caseData.setRespondent2ResponseDate(null);

        assertThat(strategy.supports(caseData)).isTrue();
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

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getDefenceFiled()).hasSize(1);
        assertThat(builder.getDefenceFiled().getFirst().getEventSequence()).isEqualTo(10);
        assertThat(builder.getDefenceFiled().getFirst().getEventCode()).isEqualTo(DEFENCE_FILED.getCode());
        assertThat(builder.getDefenceFiled().getFirst().getDateReceived()).isEqualTo(caseData.getRespondent1ResponseDate());

        assertThat(builder.getDirectionsQuestionnaireFiled()).hasSize(1);
        assertThat(builder.getDirectionsQuestionnaireFiled().getFirst().getEventSequence()).isEqualTo(11);
        assertThat(builder.getDirectionsQuestionnaireFiled().getFirst().getEventCode())
            .isEqualTo(DIRECTIONS_QUESTIONNAIRE_FILED.getCode());
        assertThat(builder.getDirectionsQuestionnaireFiled().getFirst().getEventDetailsText())
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
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getMiscellaneous()).isNullOrEmpty();
        assertThat(builder.getDefenceFiled()).hasSize(1);
        assertThat(builder.getDirectionsQuestionnaireFiled()).hasSize(1);
        assertThat(builder.getDefenceAndCounterClaim()).isNullOrEmpty();
    }

    @Test
    void contributeAddsStatesPaidWhenFirstRespondentPaysInFull() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .build();

        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(BigDecimal.valueOf(10_000));

        caseData.setTotalClaimAmount(BigDecimal.valueOf(100));
        caseData.setRespondToClaim(respondToClaim);

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getStatesPaid()).hasSize(1);
        assertThat(builder.getStatesPaid().getFirst().getEventSequence()).isEqualTo(10);
        assertThat(builder.getStatesPaid().getFirst().getEventCode()).isEqualTo(STATES_PAID.getCode());
        assertThat(builder.getDefenceFiled()).isNullOrEmpty();
    }

    @Test
    void contributeHandlesSameSolicitorSameResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateBothRespondentsSameResponse1v2SameSolicitor(FULL_DEFENCE)
            .respondentResponseIsSame(YES)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getDefenceFiled()).hasSize(2);
        assertThat(builder.getDefenceFiled())
            .extracting(Event::getEventCode)
            .containsOnly(DEFENCE_FILED.getCode());
        assertThat(builder.getDefenceFiled().getFirst().getEventSequence()).isEqualTo(10);
        assertThat(builder.getDefenceFiled().get(1).getEventSequence())
            .isGreaterThan(builder.getDefenceFiled().getFirst().getEventSequence());

        assertThat(builder.getDirectionsQuestionnaireFiled()).hasSize(2);
        assertThat(builder.getDirectionsQuestionnaireFiled())
            .extracting(Event::getEventCode)
            .containsOnly(DIRECTIONS_QUESTIONNAIRE_FILED.getCode());
    }

    @Test
    void contributeAddsStatesPaidForSecondRespondentWhenSameSolicitorPaidInFull() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateBothRespondentsSameResponse1v2SameSolicitor(FULL_DEFENCE)
            .respondentResponseIsSame(YES)
            .build();

        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(BigDecimal.valueOf(10_000));

        caseData.setTotalClaimAmount(BigDecimal.valueOf(100));
        caseData.setRespondToClaim(respondToClaim);

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13, 14);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getStatesPaid()).hasSize(2);
        assertThat(builder.getStatesPaid())
            .extracting(Event::getEventCode)
            .containsOnly(STATES_PAID.getCode());
        assertThat(builder.getStatesPaid().getFirst().getEventSequence()).isEqualTo(10);
        assertThat(builder.getStatesPaid().get(1).getEventSequence())
            .isGreaterThan(builder.getStatesPaid().getFirst().getEventSequence());
        assertThat(builder.getStatesPaid())
            .extracting(Event::getEventCode)
            .containsOnly(STATES_PAID.getCode());
        assertThat(builder.getDefenceFiled()).hasSize(1);
        assertThat(builder.getDefenceFiled().getFirst().getEventCode()).isEqualTo(DEFENCE_FILED.getCode());
    }

    @Test
    void contributeHandlesDifferentSolicitorsForRespondents() {
        CaseData caseData = StrategyTestDataFactory.unspecTwoDefendantSolicitorsCase()
            .atStateBothRespondentsSameResponse(FULL_DEFENCE)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getDefenceFiled()).hasSize(2);
        assertThat(builder.getDefenceFiled())
            .extracting(Event::getEventCode)
            .containsOnly(DEFENCE_FILED.getCode());

        assertThat(builder.getDirectionsQuestionnaireFiled()).hasSize(2);
        assertThat(builder.getDirectionsQuestionnaireFiled())
            .extracting(Event::getEventCode)
            .containsOnly(DIRECTIONS_QUESTIONNAIRE_FILED.getCode());
    }

    @Test
    void contributeAddsStatesPaidForSecondRespondentWhenDifferentSolicitorsAndPaid() {
        CaseData caseData = StrategyTestDataFactory.unspecTwoDefendantSolicitorsCase()
            .atStateBothRespondentsSameResponse(FULL_DEFENCE)
            .build();

        RespondToClaim respondToClaim2 = new RespondToClaim();
        respondToClaim2.setHowMuchWasPaid(BigDecimal.valueOf(10_000));

        caseData.setTotalClaimAmount(BigDecimal.valueOf(100));
        caseData.setRespondToClaim2(respondToClaim2);

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getStatesPaid()).hasSize(1);
        assertThat(builder.getStatesPaid().getFirst().getEventCode()).isEqualTo(STATES_PAID.getCode());
        assertThat(builder.getDefenceFiled()).hasSize(1);
        assertThat(builder.getDefenceFiled().getFirst().getEventCode()).isEqualTo(DEFENCE_FILED.getCode());
    }

    @Test
    void contributeRespectsLrVsLipStatesPaidBranch() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .applicant1Represented(YES)
            .respondent1Represented(NO)
            .build();
        caseData.setDefenceRouteRequired(SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED);

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getStatesPaid())
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

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getDefenceFiled())
            .extracting(Event::getEventSequence)
            .containsExactly(10);
        assertThat(builder.getStatesPaid()).isNullOrEmpty();
    }

    @Test
    void contributeAddsMiscEventForUnspecFullDefence() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getMiscellaneous()).isNullOrEmpty();
    }

    @Test
    void contributeDoesNotAddMiscEventForSpecLipFullDefence() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .setClaimTypeToSpecClaim()
            .applicant1Represented(NO)
            .respondent1Represented(NO)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getMiscellaneous()).isNullOrEmpty();
    }

    @Test
    void contributeHandlesSameSolicitorDivergentResponsesForSpec() {
        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .multiPartyClaimOneDefendantSolicitor()
            .atState1v2SameSolicitorDivergentResponse(FULL_ADMISSION, FULL_DEFENCE)
            .respondentResponseIsSame(NO)
            .respondent2DQ()
            .build();

        assertThat(strategy.supports(caseData)).isTrue();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getDefenceFiled()).hasSize(1);
        assertThat(builder.getDirectionsQuestionnaireFiled()).hasSize(1);
    }

    @Test
    void contributeDoesNotAddRespondent2EventsWhenNoResponseDateWithSameSolicitorDifferentResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .multiPartyClaimOneDefendantSolicitor()
            .atState1v2SameSolicitorDivergentResponse(FULL_DEFENCE, FULL_ADMISSION)
            .respondentResponseIsSame(NO)
            .respondent2ResponseDate(null)
            .build();

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getDefenceFiled()).hasSize(1);
        assertThat(builder.getDirectionsQuestionnaireFiled()).hasSize(1);
        assertThat(builder.getDefenceFiled().getFirst().getLitigiousPartyID())
            .isEqualTo("002");
    }

    @Test
    void skipsRespondent2EventsWhenResponseDateMissing() {
        CaseData caseData = StrategyTestDataFactory.unspecTwoDefendantSolicitorsCase()
            .atStateBothRespondentsSameResponse(FULL_DEFENCE)
            .build();
        caseData.setRespondent2ResponseDate(null);

        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(20, 21);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getDefenceFiled()).hasSize(1);
        assertThat(builder.getDirectionsQuestionnaireFiled()).hasSize(1);
        assertThat(builder.getDirectionsQuestionnaireFiled().getFirst().getLitigiousPartyID()).isEqualTo("002");
    }
}
