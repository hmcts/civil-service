package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

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
            .build()
            .toBuilder()
            .applicant1ResponseDate(applicantResponse)
            .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
            .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
            .build();

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
    void addsNotProceedEvent() {
        when(stateFlow.getStateHistory()).thenReturn(List.of(State.from(FlowState.Main.FULL_DEFENCE_NOT_PROCEED.fullName())));

        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atStateApplicantRespondToDefenceAndProceed(TWO_V_ONE)
            .build()
            .toBuilder()
            .applicant1ResponseDate(LocalDateTime.of(2024, 4, 5, 11, 0))
            .applicant1ProceedWithClaimMultiParty2v1(YesOrNo.NO)
            .applicant2ProceedWithClaimMultiParty2v1(YesOrNo.NO)
            .build();

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
            .build()
            .toBuilder()
            .applicant1ResponseDate(LocalDateTime.of(2024, 5, 10, 14, 0))
            .build();

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
            .build()
            .toBuilder()
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
            .applicant1ResponseDate(LocalDateTime.of(2024, 3, 21, 9, 0))
            .applicant2ResponseDate(LocalDateTime.of(2024, 3, 21, 9, 0))
            .build();

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
            .build()
            .toBuilder()
            .applicant1ResponseDate(applicantResponse)
            .build();

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
            .build()
            .toBuilder()
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .addRespondent2(YesOrNo.YES)
            .respondent2(uk.gov.hmcts.reform.civil.sampledata.PartyBuilder.builder().individual().build())
            .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.YES)
            .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.NO)
            .applicant1ResponseDate(LocalDateTime.of(2024, 7, 1, 11, 0))
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .build();

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
            .build()
            .toBuilder()
            .applicant1ResponseDate(LocalDateTime.of(2024, 8, 15, 10, 0))
            .build();

        EventHistory.EventHistoryBuilder builder = EventHistory.builder();
        strategy.contribute(builder, caseData, "token");

        EventHistory history = builder.build();
        assertThat(history.getMiscellaneous()).hasSize(1);
        assertThat(history.getMiscellaneous().get(0).getEventDetailsText())
            .contains("Claimant proceeds");
    }
}
