package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
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

class RespondentDivergentResponseStrategyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2024, 6, 20, 9, 15);

    @Mock private RoboticsSequenceGenerator sequenceGenerator;

    @Mock private IStateFlowEngine stateFlowEngine;

    @Mock private StateFlow stateFlow;

    private RoboticsRespondentResponseSupport respondentResponseSupport;
    private RespondentDivergentResponseStrategy strategy;
    private final Logger logger = (Logger) LoggerFactory.getLogger(RespondentDivergentResponseStrategy.class);
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
        RoboticsEventTextFormatter formatter = new RoboticsEventTextFormatter();
        RoboticsTimelineHelper timelineHelper = new RoboticsTimelineHelper(() -> NOW);
        respondentResponseSupport = new RoboticsRespondentResponseSupport(formatter, timelineHelper);
        strategy =
                new RespondentDivergentResponseStrategy(
                        sequenceGenerator, respondentResponseSupport, stateFlowEngine);

        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(logAppender);
        logAppender.stop();
    }

    @Test
    void supportsReturnsFalseWhenStateMissing() {
        when(stateFlow.getStateHistory())
                .thenReturn(List.of(State.from(FlowState.Main.CLAIM_ISSUED.fullName())));

        CaseData caseData = createUnspecDivergentCase();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsFalseWhenCaseDataNull() {
        assertThat(strategy.supports(null)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenStatePresentEvenIfResponsesMissing() {
        when(stateFlow.getStateHistory())
                .thenReturn(
                        List.of(
                                State.from(
                                        FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED
                                                .fullName())));

        CaseData caseData = CaseDataBuilder.builder().build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void supportsReturnsTrueWhenStatePresentAndResponsesExist() {
        when(stateFlow.getStateHistory())
                .thenReturn(
                        List.of(
                                State.from(FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName())));

        CaseData caseData = createUnspecDivergentCase();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void contributeDoesNothingWhenNotSupported() {
        when(stateFlow.getStateHistory())
                .thenReturn(List.of(State.from(FlowState.Main.CLAIM_ISSUED.fullName())));
        when(stateFlow.getState()).thenReturn(State.from(FlowState.Main.CLAIM_ISSUED.fullName()));

        CaseData caseData = createUnspecDivergentCase();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getDefenceFiled()).isNullOrEmpty();
        assertThat(builder.getDirectionsQuestionnaireFiled()).isNullOrEmpty();
        assertThat(builder.getReceiptOfPartAdmission()).isNullOrEmpty();
        assertThat(builder.getMiscellaneous()).isNullOrEmpty();
        verifyNoInteractions(sequenceGenerator);
    }

    @Test
    void contributeAddsEventsForUnspecDivergentResponses() {
        when(stateFlow.getStateHistory())
                .thenReturn(
                        List.of(
                                State.from(
                                        FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED
                                                .fullName())));
        when(stateFlow.getState())
                .thenReturn(
                        State.from(
                                FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED
                                        .fullName()));
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(10, 11, 12, 13);

        CaseData caseData = createUnspecDivergentCase();

        assertThat(strategy.supports(caseData)).isTrue();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getDefenceFiled()).extracting(Event::getEventSequence).containsExactly(10);

        assertThat(builder.getDirectionsQuestionnaireFiled())
                .extracting(Event::getEventSequence)
                .containsExactly(11);

        assertThat(builder.getReceiptOfPartAdmission())
                .extracting(Event::getEventSequence)
                .containsExactly(12);

        assertThat(builder.getMiscellaneous()).extracting(Event::getEventSequence).containsExactly(13);

        assertThat(builder.getMiscellaneous())
                .filteredOn(event -> event.getEventCode() != null)
                .extracting(Event::getEventDetails)
                .extracting(EventDetails::getMiscText)
                .containsExactly(
                        respondentResponseSupport.prepareRespondentResponseText(
                                caseData, caseData.getRespondent2(), false));
    }

    @Test
    void contributeUsesStateFlowWhenFlowStateNull() {
        when(stateFlow.getStateHistory())
                .thenReturn(
                        List.of(State.from(FlowState.Main.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED.fullName())));
        when(stateFlow.getState())
                .thenReturn(State.from(FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE.fullName()));
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(70, 71, 72, 73);

        CaseData caseData = createSpecDivergentCase();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null, null);

        assertThat(builder.getMiscellaneous()).hasSize(1);
    }

    @Test
    void contributeAddsSpecMiscOnlyWhenOffline() {
        when(stateFlow.getStateHistory())
                .thenReturn(
                        List.of(
                                State.from(FlowState.Main.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED.fullName()),
                                State.from(FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE.fullName())));
        when(stateFlow.getState())
                .thenReturn(State.from(FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE.fullName()));
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(20, 21, 22, 23);

        CaseData caseData = createSpecDivergentCase();

        assertThat(strategy.supports(caseData)).isTrue();

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getReceiptOfPartAdmission())
                .extracting(Event::getEventSequence)
                .containsExactly(20);

        List<String> miscTexts =
                builder.getMiscellaneous().stream()
                        .filter(event -> event != null && event.getEventCode() != null)
                        .map(Event::getEventDetailsText)
                        .toList();
        assertThat(miscTexts)
                .containsExactly(
                        respondentResponseSupport.prepareRespondentResponseText(
                                caseData, caseData.getRespondent1(), true));

        assertThat(builder.getDefenceFiled()).extracting(Event::getEventSequence).containsExactly(22);

        assertThat(builder.getDirectionsQuestionnaireFiled())
                .extracting(Event::getLitigiousPartyID)
                .containsExactly("003");
    }

    @Test
    void addsStatesPaidWhenRespondentPaysInFull() {
        when(stateFlow.getStateHistory())
                .thenReturn(
                        List.of(
                                State.from(FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName())));
        when(stateFlow.getState())
                .thenReturn(State.from(FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName()));
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(30, 31);

        BigDecimal claimAmount = BigDecimal.valueOf(1000);
        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(claimAmount.multiply(BigDecimal.valueOf(100)));
        respondToClaim.setWhenWasThisAmountPaid(LocalDate.now());

        CaseData caseData = createUnspecDivergentCase();
        caseData.setTotalClaimAmount(claimAmount);
        caseData.setRespondToClaim(respondToClaim);
        caseData.setRespondent1ClaimResponseType(RespondentResponseType.FULL_DEFENCE);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getStatesPaid()).hasSize(1);
        assertThat(builder.getDefenceFiled()).isNullOrEmpty();
    }

    @Test
    void doesNotAddMiscForSpecFullDefenceWhenGoingOffline() {
        when(stateFlow.getStateHistory())
                .thenReturn(
                        List.of(
                                State.from(FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName()),
                                State.from(FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE.fullName())));
        when(stateFlow.getState())
                .thenReturn(State.from(FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE.fullName()));
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(40, 41);

        CaseData caseData =
                CaseDataBuilder.builder()
                        .respondent1DQ()
                        .setClaimTypeToSpecClaim()
                        .respondent1(createIndividualParty("One"))
                        .build();
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setRespondent1ResponseDate(NOW);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getMiscellaneous()).isNullOrEmpty();
        assertThat(builder.getDirectionsQuestionnaireFiled()).hasSize(1);
    }

    @Test
    void usesRespondent1ResponseDateWhenSameSolicitor() {
        when(stateFlow.getStateHistory())
                .thenReturn(
                        List.of(
                                State.from(FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE.fullName())));
        when(stateFlow.getState())
                .thenReturn(State.from(FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE.fullName()));
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(50, 51, 52, 53);

        CaseData caseData =
                CaseDataBuilder.builder()
                        .atState1v2SameSolicitorDivergentResponse(
                                RespondentResponseType.FULL_DEFENCE, RespondentResponseType.PART_ADMISSION)
                        .build();
        caseData.setRespondent1(createIndividualParty("One"));
        caseData.setRespondent2(createIndividualParty("Two"));
        caseData.setRespondent1ResponseDate(NOW);
        caseData.setRespondent2ResponseDate(NOW.plusDays(5));
        caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
        caseData.setSameSolicitorSameResponse(YesOrNo.YES);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        LocalDateTime expectedDate = caseData.getRespondent1ResponseDate();
        assertThat(builder.getDirectionsQuestionnaireFiled())
                .extracting(Event::getDateReceived)
                .contains(expectedDate);
        assertThat(builder.getMiscellaneous())
                .extracting(Event::getDateReceived)
                .contains(expectedDate);
    }

    @Test
    void addsReceiptOfAdmissionForFullAdmissionResponse() {
        when(stateFlow.getStateHistory())
                .thenReturn(
                        List.of(State.from(FlowState.Main.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED.fullName())));
        when(stateFlow.getState())
                .thenReturn(State.from(FlowState.Main.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED.fullName()));
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(60, 61, 62, 63);

        CaseData caseData = createUnspecDivergentCase();
        caseData.setRespondent2ClaimResponseType(RespondentResponseType.FULL_ADMISSION);
        caseData.setRespondent2ResponseDate(NOW.plusDays(3));

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getReceiptOfAdmission()).hasSize(1);
        assertThat(builder.getReceiptOfAdmission().getFirst().getLitigiousPartyID()).isEqualTo("003");
    }

    @Test
    void usesClaimantResponseTypeForTwoVOneSpec() {
        when(stateFlow.getStateHistory())
                .thenReturn(
                        List.of(State.from(FlowState.Main.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED.fullName())));
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(80);

        CaseData caseData =
                CaseDataBuilder.builder()
                        .setClaimTypeToSpecClaim()
                        .respondent1(createIndividualParty("Solo"))
                        .respondent1ResponseDate(NOW)
                        .build();
        caseData.setAddApplicant2(YesOrNo.YES);
        caseData.setClaimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);

        EventHistory builder = new EventHistory();
        strategy.contribute(
                builder, caseData, null, FlowState.Main.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED);

        assertThat(builder.getReceiptOfAdmission()).hasSize(1);
        assertThat(builder.getDefenceFiled()).isNullOrEmpty();
        assertThat(builder.getDirectionsQuestionnaireFiled()).isNullOrEmpty();
    }

    @Test
    void usesRespondent1ResponseWhenSameSolicitorSameResponseForRespondent2() {
        when(stateFlow.getStateHistory())
                .thenReturn(
                        List.of(
                                State.from(FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName())));
        when(stateFlow.getState())
                .thenReturn(State.from(FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName()));
        when(sequenceGenerator.nextSequence(any(EventHistory.class)))
                .thenReturn(90, 91, 92, 93, 94, 95);

        CaseData caseData = CaseDataBuilder.builder().multiPartyClaimOneDefendantSolicitor().build();
        caseData.setRespondent1(createIndividualParty("One"));
        caseData.setRespondent2(createIndividualParty("Two"));
        caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
        caseData.setRespondentResponseIsSame(YesOrNo.YES);
        caseData.setSameSolicitorSameResponse(YesOrNo.YES);
        caseData.setRespondent1ResponseDate(NOW);
        caseData.setRespondent2ResponseDate(NOW.plusDays(1));
        caseData.setRespondent1ClaimResponseType(RespondentResponseType.FULL_DEFENCE);
        caseData.setRespondent2ClaimResponseType(RespondentResponseType.FULL_DEFENCE);
        caseData.setTotalClaimAmount(BigDecimal.valueOf(100));

        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(BigDecimal.valueOf(10000));
        respondToClaim.setWhenWasThisAmountPaid(LocalDate.now());
        caseData.setRespondToClaim(respondToClaim);

        RespondToClaim respondToClaim2 = new RespondToClaim();
        respondToClaim2.setHowMuchWasPaid(BigDecimal.ZERO);
        caseData.setRespondToClaim2(respondToClaim2);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getStatesPaid()).extracting(Event::getLitigiousPartyID).contains("003");
    }

    @Test
    void addsDefenceFiledWhenPaidLessThanClaimForRespondent2() {
        when(stateFlow.getStateHistory())
                .thenReturn(
                        List.of(
                                State.from(FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName())));
        when(stateFlow.getState())
                .thenReturn(State.from(FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED.fullName()));
        when(sequenceGenerator.nextSequence(any(EventHistory.class)))
                .thenReturn(100, 101, 102, 103, 104, 105);

        CaseData caseData = CaseDataBuilder.builder().multiPartyClaimOneDefendantSolicitor().build();
        caseData.setRespondent1(createIndividualParty("One"));
        caseData.setRespondent2(createIndividualParty("Two"));
        caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
        caseData.setRespondentResponseIsSame(YesOrNo.YES);
        caseData.setSameSolicitorSameResponse(YesOrNo.YES);
        caseData.setRespondent1ResponseDate(NOW);
        caseData.setRespondent2ResponseDate(NOW.plusDays(1));
        caseData.setRespondent1ClaimResponseType(RespondentResponseType.FULL_DEFENCE);
        caseData.setRespondent2ClaimResponseType(RespondentResponseType.FULL_DEFENCE);
        caseData.setTotalClaimAmount(BigDecimal.valueOf(100));

        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(BigDecimal.valueOf(5000));
        respondToClaim.setWhenWasThisAmountPaid(LocalDate.now());
        caseData.setRespondToClaim(respondToClaim);

        RespondToClaim respondToClaim2 = new RespondToClaim();
        respondToClaim2.setHowMuchWasPaid(BigDecimal.valueOf(0));
        caseData.setRespondToClaim2(respondToClaim2);

        EventHistory builder = new EventHistory();
        strategy.contribute(builder, caseData, null);

        assertThat(builder.getDefenceFiled()).extracting(Event::getLitigiousPartyID).contains("003");
    }

    @ParameterizedTest
    @EnumSource(value = CaseCategory.class, names = "UNSPEC_CLAIM")
    @NullSource
    void skipsAllResponseEventsWhenOnlyRespondentHasNoResponseType(CaseCategory category) {
        givenDivergentState();
        CaseData caseData = CaseDataBuilder.builder()
                .caseReference(1234567890123456L)
                .respondent1ResponseDate(NOW)
                .build();
        caseData.setCaseAccessCategory(category);
        caseData.setRespondent1ClaimResponseType(null);

        EventHistory eventHistory = new EventHistory();
        strategy.contribute(eventHistory, caseData, null);

        assertThat(eventHistory.getDefenceFiled()).isNullOrEmpty();
        assertThat(eventHistory.getStatesPaid()).isNullOrEmpty();
        assertThat(eventHistory.getDirectionsQuestionnaireFiled()).isNullOrEmpty();
        assertThat(eventHistory.getReceiptOfAdmission()).isNullOrEmpty();
        assertThat(eventHistory.getReceiptOfPartAdmission()).isNullOrEmpty();
        assertThat(eventHistory.getMiscellaneous()).isNullOrEmpty();
        verifyNoInteractions(sequenceGenerator);
        assertMissingResponseWarning("respondent 002", "respondent1ClaimResponseType");
    }

    @ParameterizedTest
    @EnumSource(value = CaseCategory.class, names = "UNSPEC_CLAIM")
    @NullSource
    void preservesRespondent1EventsWhenRespondent2ResponseTypeMissing(CaseCategory category) {
        givenDivergentState();
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2);
        CaseData caseData = createUnspecDivergentCase();
        caseData.setCaseAccessCategory(category);
        caseData.setCcdCaseReference(1234567890123456L);
        caseData.setRespondent2ClaimResponseType(null);

        EventHistory eventHistory = new EventHistory();
        strategy.contribute(eventHistory, caseData, null);

        assertThat(eventHistory.getDefenceFiled()).extracting(Event::getLitigiousPartyID).containsExactly("002");
        assertThat(eventHistory.getDirectionsQuestionnaireFiled())
                .extracting(Event::getLitigiousPartyID).containsExactly("002");
        assertThat(eventHistory.getReceiptOfAdmission()).isNullOrEmpty();
        assertThat(eventHistory.getReceiptOfPartAdmission()).isNullOrEmpty();
        assertThat(eventHistory.getMiscellaneous()).isNullOrEmpty();
        assertMissingResponseWarning("respondent 003", "respondent2ClaimResponseType");
    }

    @ParameterizedTest
    @EnumSource(value = CaseCategory.class, names = "UNSPEC_CLAIM")
    @NullSource
    void preservesRespondent2EventsWhenRespondent1ResponseTypeMissing(CaseCategory category) {
        givenDivergentState();
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2);
        CaseData caseData = createUnspecDivergentCase();
        caseData.setCaseAccessCategory(category);
        caseData.setCcdCaseReference(1234567890123456L);
        caseData.setRespondent1ClaimResponseType(null);

        EventHistory eventHistory = new EventHistory();
        strategy.contribute(eventHistory, caseData, null);

        assertThat(eventHistory.getDefenceFiled()).isNullOrEmpty();
        assertThat(eventHistory.getDirectionsQuestionnaireFiled()).isNullOrEmpty();
        assertThat(eventHistory.getReceiptOfPartAdmission())
                .extracting(Event::getLitigiousPartyID).containsExactly("003");
        assertThat(eventHistory.getMiscellaneous()).extracting(Event::getEventDetailsText)
                .containsExactly(respondentResponseSupport.prepareRespondentResponseText(
                        caseData, caseData.getRespondent2(), false));
        assertMissingResponseWarning("respondent 002", "respondent1ClaimResponseType");
    }

    @Test
    void skipsRespondent2WithoutResponseTypeWhenDivergentResponseUsesSameSolicitor() {
        givenDivergentState();
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenReturn(1, 2);
        CaseData caseData = createUnspecDivergentCase();
        caseData.setCcdCaseReference(1234567890123456L);
        caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
        caseData.setRespondentResponseIsSame(YesOrNo.NO);
        caseData.setRespondent2ResponseDate(null);
        caseData.setRespondent2ClaimResponseType(null);

        EventHistory eventHistory = new EventHistory();
        strategy.contribute(eventHistory, caseData, null);

        assertThat(eventHistory.getDefenceFiled()).extracting(Event::getLitigiousPartyID).containsExactly("002");
        assertThat(eventHistory.getDirectionsQuestionnaireFiled())
                .extracting(Event::getLitigiousPartyID).containsExactly("002");
        assertThat(eventHistory.getReceiptOfPartAdmission()).isNullOrEmpty();
        assertThat(eventHistory.getMiscellaneous()).isNullOrEmpty();
        assertMissingResponseWarning("respondent 003", "respondent2ClaimResponseType");
    }

    @Test
    void doesNotWarnWhenNeitherRespondentHasResponded() {
        givenDivergentState();
        CaseData caseData = createUnspecDivergentCase();
        caseData.setRespondent1ResponseDate(null);
        caseData.setRespondent2ResponseDate(null);
        caseData.setRespondent1ClaimResponseType(null);
        caseData.setRespondent2ClaimResponseType(null);

        EventHistory eventHistory = new EventHistory();
        strategy.contribute(eventHistory, caseData, null);

        verifyNoInteractions(sequenceGenerator);
        assertThat(logAppender.list).noneMatch(event -> event.getLevel() == Level.WARN);
    }

    private void givenDivergentState() {
        State state = State.from(FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE.fullName());
        when(stateFlow.getStateHistory()).thenReturn(List.of(state));
        when(stateFlow.getState()).thenReturn(state);
    }

    private void assertMissingResponseWarning(String respondent, String field) {
        assertThat(logAppender.list).filteredOn(event -> event.getLevel() == Level.WARN)
                .singleElement().satisfies(event -> assertThat(event.getFormattedMessage())
                        .contains("1234567890123456", respondent, field));
    }

    private CaseData createUnspecDivergentCase() {
        return StrategyTestDataFactory.unspecTwoDefendantSolicitorsCase()
                .respondent1ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
                .respondent2ClaimResponseType(RespondentResponseType.PART_ADMISSION)
                .respondent1ResponseDate(NOW)
                .respondent2ResponseDate(NOW.plusDays(1))
                .build();
    }

    private CaseData createSpecDivergentCase() {
        CaseDataBuilder builder = StrategyTestDataFactory.specTwoDefendantSolicitorsCase();
        Party respondent1 = createIndividualParty("One");
        Party respondent2 = createIndividualParty("Two");
        builder.respondent1(respondent1);
        builder.respondent2(respondent2);
        return builder
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .respondent1ResponseDate(NOW)
                .respondent2ResponseDate(NOW.plusDays(2))
                .build();
    }

    private Party createIndividualParty(String lastName) {
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        party.setIndividualFirstName("Respondent");
        party.setIndividualLastName(lastName);
        return party;
    }
}
