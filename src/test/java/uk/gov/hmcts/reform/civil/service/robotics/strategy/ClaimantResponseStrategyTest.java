package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.FileDirectionsQuestionnaire;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT2_ID;

class ClaimantResponseStrategyTest {

    @Mock
    private IStateFlowEngine stateFlowEngine;
    @Mock
    private StateFlow stateFlow;
    @Mock
    private RoboticsSequenceGenerator sequenceGenerator;
    @Spy
    private RoboticsEventTextFormatter textFormatter = new RoboticsEventTextFormatter();
    @Mock
    private RoboticsTimelineHelper timelineHelper;
    @Mock
    private LocationRefDataUtil locationRefDataUtil;

    @InjectMocks
    private ClaimantResponseStrategy strategy;

    private int sequence;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sequence = 1;
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(timelineHelper.now()).thenReturn(LocalDateTime.of(2024, 3, 20, 10, 0));
        when(sequenceGenerator.nextSequence(any(EventHistory.class))).thenAnswer(invocation -> sequence++);
        when(locationRefDataUtil.getPreferredCourtData(any(), any(), any(Boolean.class))).thenReturn("PREF001");
    }

    @Test
    void supportsReturnsFalseWhenStateNotPresent() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.CLAIM_ISSUED.fullName())));
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed(ONE_V_ONE)
            .build();

        assertThat(strategy.supports(caseData)).isFalse();
    }

    @Test
    void supportsReturnsTrueWhenProceedStatePresent() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.FULL_DEFENCE_PROCEED.fullName())));
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed(ONE_V_ONE)
            .build();

        assertThat(strategy.supports(caseData)).isTrue();
    }

    @Test
    void addsProceedEventsForUnspecClaim() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.FULL_DEFENCE_PROCEED.fullName())));

        LocalDateTime applicantResponse = LocalDateTime.of(2024, 3, 18, 9, 0);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed(ONE_V_ONE)
            .build();
        caseData.setApplicant1ResponseDate(applicantResponse);
        caseData.setAllocatedTrack(AllocatedTrack.MULTI_CLAIM);
        caseData.setResponseClaimTrack(AllocatedTrack.FAST_CLAIM.name());

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, "auth-token");

        EventHistory history = builder.build();
        assertThat(history.getDirectionsQuestionnaireFiled()).hasSize(1);
        assertThat(history.getDirectionsQuestionnaireFiled().get(0).getEventCode())
            .isEqualTo(EventType.DIRECTIONS_QUESTIONNAIRE_FILED.getCode());

        assertThat(history.getMiscellaneous()).extracting(Event::getEventDetailsText)
            .contains("Claimant proceeds.", "RPA Reason:Multitrack Unspec going offline.");
    }

    @Test
    void keepsNullPreferredCourtCodeWhenLocationOverrideMissing() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.FULL_DEFENCE_PROCEED.fullName())));
        when(locationRefDataUtil.getPreferredCourtData(any(), any(), any(Boolean.class))).thenReturn(null);

        FileDirectionsQuestionnaire questionnaire = new FileDirectionsQuestionnaire();
        questionnaire.setOneMonthStayRequested(YesOrNo.YES);
        RequestedCourt requestedCourt = new RequestedCourt();
        requestedCourt.setResponseCourtCode("123");
        Applicant1DQ applicant1DQ = new Applicant1DQ();
        applicant1DQ.setApplicant1DQFileDirectionsQuestionnaire(questionnaire);
        applicant1DQ.setApplicant1DQRequestedCourt(requestedCourt);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed(ONE_V_ONE)
            .build();
        caseData.setApplicant1ResponseDate(LocalDateTime.of(2024, 3, 18, 9, 0));
        caseData.setApplicant1DQ(applicant1DQ);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, "auth-token");

        Event dqEvent = builder.build().getDirectionsQuestionnaireFiled().get(0);
        assertThat(dqEvent.getEventDetails().getPreferredCourtCode()).isNull();
        assertThat(dqEvent.getEventDetailsText()).isEqualTo("preferredCourtCode: null; stayClaim: true");
    }

    @Test
    void addsNotProceedEvent() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.FULL_DEFENCE_NOT_PROCEED.fullName())));

        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atStateApplicantRespondToDefenceAndProceed(TWO_V_ONE)
            .build();
        caseData.setApplicant1ResponseDate(LocalDateTime.of(2024, 4, 5, 11, 0));
        caseData.setApplicant1ProceedWithClaimMultiParty2v1(YesOrNo.NO);
        caseData.setApplicant2ProceedWithClaimMultiParty2v1(YesOrNo.NO);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, null);

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).extracting(Event::getEventDetailsText)
            .containsExactly("RPA Reason: Claimants intend not to proceed.");
    }

    @Test
    void addsMediationTextsForSmallClaimMediationScenario() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.FULL_DEFENCE_PROCEED.fullName())));

        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantProceedAllMediation(ONE_V_TWO_TWO_LEGAL_REP)
            .build();
        caseData.setApplicant1ResponseDate(LocalDateTime.of(2024, 5, 10, 14, 0));

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, "token");

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(2);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .contains("Claimant has provided intention");
    }

    @Test
    void addsMixedMultipartyTextsWhenOnlySecondApplicantProceeds() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.FULL_DEFENCE_PROCEED.fullName())));

        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atStateApplicant2RespondToDefenceAndProceed_2v1()
            .build();
        caseData.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);
        caseData.setResponseClaimTrack(AllocatedTrack.FAST_CLAIM.name());
        caseData.setApplicant1ResponseDate(LocalDateTime.of(2024, 3, 21, 9, 0));
        caseData.setApplicant2ResponseDate(LocalDateTime.of(2024, 3, 21, 9, 0));

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, "token");

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).extracting(Event::getEventDetailsText)
            .anySatisfy(text -> assertThat(text).contains("[1 of 2"))
            .anySatisfy(text -> assertThat(text).contains("[2 of 2"));
        assertThat(history.getDirectionsQuestionnaireFiled()).extracting(Event::getLitigiousPartyID)
            .contains(APPLICANT2_ID);
    }

    @Test
    void addsSpecDirectionsQuestionnaireEvents() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.FULL_DEFENCE_PROCEED.fullName())));

        LocalDateTime applicantResponse = LocalDateTime.of(2024, 6, 12, 9, 30);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed(ONE_V_ONE)
            .setClaimTypeToSpecClaim()
            .build();
        caseData.setApplicant1ResponseDate(applicantResponse);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, "token");

        EventHistory history = builder.build();
        assertThat(history.getDirectionsQuestionnaireFiled()).hasSize(1);
        Event dqEvent = history.getDirectionsQuestionnaireFiled().get(0);
        String expectedCourt = uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport
            .getPreferredCourtCode(caseData.getApplicant1DQ());
        assertThat(dqEvent.getEventDetails().getPreferredCourtCode()).isEqualTo(expectedCourt);
        assertThat(dqEvent.getEventDetailsText()).contains("preferredCourtCode");
    }

    @Test
    void usesMultipartyTextsWhenOnlyOneDefendantProceedsWithSameSolicitor() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.FULL_DEFENCE_PROCEED.fullName())));

        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceedVsDefendant1Only_1v2()
            .build();
        caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
        caseData.setAddRespondent2(YesOrNo.YES);
        caseData.setRespondent2(createIndividualParty());
        caseData.setApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.YES);
        caseData.setApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.NO);
        caseData.setApplicant1ResponseDate(LocalDateTime.of(2024, 7, 1, 11, 0));
        caseData.setAllocatedTrack(AllocatedTrack.FAST_CLAIM);

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, "token");

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(2);
        assertThat(history.getMiscellaneous())
            .extracting(Event::getEventDetailsText)
            .allSatisfy(text -> assertThat(text).contains("Claimant has provided intention"));
        assertThat(history.getMiscellaneous())
            .map(Event::getEventDetailsText)
            .noneMatch(text -> text.contains("Multitrack"));
    }

    @Test
    void doesNotAddTakenOfflineWhenSmallClaimMediationOneVOne() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.FULL_DEFENCE_PROCEED.fullName())));

        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantProceedAllMediation(ONE_V_ONE)
            .build();
        caseData.setApplicant1ResponseDate(LocalDateTime.of(2024, 8, 15, 10, 0));

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, "token");

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .contains("Claimant proceeds");
    }

    private Party createIndividualParty() {
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        party.setIndividualFirstName("Alex");
        party.setIndividualLastName("Jones");
        return party;
    }
}
