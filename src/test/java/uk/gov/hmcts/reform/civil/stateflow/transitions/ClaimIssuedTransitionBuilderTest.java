package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimIssuedTransitionBuilder.awaitingResponsesFullDefenceReceivedSpec;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimIssuedTransitionBuilder.awaitingResponsesNonFullDefenceReceivedSpec;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimIssuedTransitionBuilder.claimNotified;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimIssuedTransitionBuilder.contactDetailsChange;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimIssuedTransitionBuilder.pastClaimNotificationDeadline;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimIssuedTransitionBuilder.takenOfflineAfterClaimNotified;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimIssuedTransitionBuilder.takenOfflineByStaffAfterClaimIssue;

@ExtendWith(MockitoExtension.class)
public class ClaimIssuedTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        ClaimIssuedTransitionBuilder claimIssuedTransitionBuilder = new ClaimIssuedTransitionBuilder(
            mockFeatureToggleService);
        result = claimIssuedTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(14);

        assertTransition(result.get(0), "MAIN.CLAIM_ISSUED", "MAIN.CLAIM_NOTIFIED");
        assertTransition(result.get(1), "MAIN.CLAIM_ISSUED", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(2), "MAIN.CLAIM_ISSUED", "MAIN.TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED");
        assertTransition(result.get(3), "MAIN.CLAIM_ISSUED", "MAIN.PAST_CLAIM_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA");
        assertTransition(result.get(4), "MAIN.CLAIM_ISSUED", "MAIN.CONTACT_DETAILS_CHANGE");
        assertTransition(result.get(5), "MAIN.CLAIM_ISSUED", "MAIN.RESPONDENT_RESPONSE_LANGUAGE_IS_BILINGUAL");
        assertTransition(result.get(6), "MAIN.CLAIM_ISSUED", "MAIN.FULL_DEFENCE");
        assertTransition(result.get(7), "MAIN.CLAIM_ISSUED", "MAIN.PART_ADMISSION");
        assertTransition(result.get(8), "MAIN.CLAIM_ISSUED", "MAIN.FULL_ADMISSION");
        assertTransition(result.get(9), "MAIN.CLAIM_ISSUED", "MAIN.COUNTER_CLAIM");
        assertTransition(result.get(10), "MAIN.CLAIM_ISSUED", "MAIN.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED");
        assertTransition(result.get(11), "MAIN.CLAIM_ISSUED", "MAIN.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED");
        assertTransition(result.get(12), "MAIN.CLAIM_ISSUED", "MAIN.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE");
        assertTransition(result.get(13), "MAIN.CLAIM_ISSUED", "MAIN.DIVERGENT_RESPOND_GO_OFFLINE");
    }

    @Test
    void shouldReturnTrueWhenCaseDataIsAtClaimNotifiedState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();
        assertTrue(claimNotified.test(caseData));
    }

    @Test
    void shouldReturnFalseWhenCaseDataIsAtDraftState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
        assertFalse(claimNotified.test(caseData));
    }

    @Test
    void shouldReturnTrueWhenSolicitorOptionsAreNullIn1v1Case() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();
        assertTrue(claimNotified.test(caseData));
    }

    @Test
    void shouldReturnTrueWhenNotifyBothSolicitorsIn1v2Case() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyBothSolicitors().build();
        assertTrue(claimNotified.test(caseData));
        assertFalse(takenOfflineAfterClaimNotified.test(caseData));
    }

    @Test
    void shouldReturnTrueWhenNotifyOneSolicitorIn1v2Case() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor().build();
        assertFalse(claimNotified.test(caseData));
        assertTrue(takenOfflineAfterClaimNotified.test(caseData));
    }

    @Test
    void shouldHandleOfflineWhenNotifyOneSolicitorIn1v2Case() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor().build();
        assertFalse(claimNotified.test(caseData));
        assertTrue(takenOfflineAfterClaimNotified.test(caseData));
    }

    @Test
    void shouldReturnTrueWhenCaseDataIsAtStateTakenOfflineAfterClaimIssue() {
        CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff().build();
        assertTrue(takenOfflineByStaffAfterClaimIssue.test(caseData));
    }

    @Test
    void shouldReturnTrueWhenCaseDataIsAtStateTakenOfflineAfterClaimIssueSpec() {
        CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff()
            .setClaimNotificationDate().setClaimTypeToSpecClaim().build();
        assertTrue(takenOfflineByStaffAfterClaimIssue.test(caseData));
    }

    @Test
    void shouldReturnTrueWhenCaseDataIsAtStateClaimDismissedPastClaimNotificationDeadline() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastClaimNotificationDeadline().build();
        assertTrue(pastClaimNotificationDeadline.test(caseData));
    }

    @Test
    void shouldReturnFalseWhenCaseDataIsAtStateAwaitingCaseNotification() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
        assertFalse(pastClaimNotificationDeadline.test(caseData));
    }

    @Test
    void shouldReturnTrueWhenAwaitingResponsesFullDefenceReceivedForRespondent1() {
        CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoDefendantSolicitors()
            .setClaimTypeToSpecClaim().respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE).build();
        assertTrue(awaitingResponsesFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void shouldReturnTrueWhenAwaitingResponsesFullDefenceReceivedForRespondent2() {
        CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoDefendantSolicitors()
            .setClaimTypeToSpecClaim().respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE).build();
        assertTrue(awaitingResponsesFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void shouldReturnFalseWhenBothRespondentsFullDefenceReceived() {
        CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoDefendantSolicitors()
            .setClaimTypeToSpecClaim().respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE).build();
        assertFalse(awaitingResponsesFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void shouldReturnFalseWhenSingleDefendantFullDefenceReceived() {
        CaseData caseData = CaseDataBuilder.builder().multiPartyClaimOneDefendantSolicitor()
            .setClaimTypeToSpecClaim().build();
        assertFalse(awaitingResponsesFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void shouldReturnTrueWhenAwaitingFirstDefendantNonFullDefenceReceived() {
        CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoDefendantSolicitors()
            .setClaimTypeToSpecClaim().respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION).build();
        assertTrue(awaitingResponsesNonFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void shouldReturnTrueWhenAwaitingSecondDefendantNonFullDefenceReceived() {
        CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoDefendantSolicitors()
            .setClaimTypeToSpecClaim().respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION).build();
        assertTrue(awaitingResponsesNonFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void shouldReturnFalseWhenNoDefendantNonFullDefenceReceived() {
        CaseData caseData = CaseDataBuilder.builder().multiPartyClaimTwoDefendantSolicitors()
            .setClaimTypeToSpecClaim().build();
        assertFalse(awaitingResponsesNonFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void shouldReturnFalseWhenSingleDefendantNonFullDefenceReceived() {
        CaseData caseData = CaseDataBuilder.builder().multiPartyClaimOneDefendantSolicitor()
            .setClaimTypeToSpecClaim().build();
        assertFalse(awaitingResponsesNonFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenContactDetailsChangedAlready() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .atSpecAoSApplicantCorrespondenceAddressRequired(NO).build();

        assertTrue(contactDetailsChange.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenContactDetailsNotYetChanged() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued().atSpecAoSApplicantCorrespondenceAddressRequired(YES).build();

        assertFalse(contactDetailsChange.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
