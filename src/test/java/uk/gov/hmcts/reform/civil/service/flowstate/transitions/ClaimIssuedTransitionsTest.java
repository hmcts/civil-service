package uk.gov.hmcts.reform.civil.service.flowstate.transitions;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.service.flowstate.transitions.ClaimIssuedTransitions.awaitingResponsesFullDefenceReceivedSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.transitions.ClaimIssuedTransitions.awaitingResponsesNonFullDefenceReceivedSpec;
import static uk.gov.hmcts.reform.civil.service.flowstate.transitions.ClaimIssuedTransitions.claimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.transitions.ClaimIssuedTransitions.pastClaimNotificationDeadline;
import static uk.gov.hmcts.reform.civil.service.flowstate.transitions.ClaimIssuedTransitions.takenOfflineAfterClaimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.transitions.ClaimIssuedTransitions.takenOfflineByStaffAfterClaimIssue;

class ClaimIssuedTransitionsTest {

    @Test
    void awaitingRespondent1ResponsesFullDefenceReceivedShouldReturnTrue() {
        CaseDataBuilder caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimTwoDefendantSolicitors().setClaimTypeToSpecClaim();
        CaseData caseData = caseDataBuilder
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        assertTrue(awaitingResponsesFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void awaitingRespondent2ResponsesFullDefenceReceivedShouldReturnTrue() {
        CaseDataBuilder caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimTwoDefendantSolicitors().setClaimTypeToSpecClaim();
        CaseData caseData = caseDataBuilder
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        assertTrue(awaitingResponsesFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void awaitingResponsesFullDefenceReceivedShouldReturnFalse() {
        CaseDataBuilder caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimTwoDefendantSolicitors().setClaimTypeToSpecClaim();
        CaseData caseData = caseDataBuilder
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        assertFalse(awaitingResponsesFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void awaitingResponsesFullDefenceReceivedShouldHitDefault() {
        CaseDataBuilder caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimOneDefendantSolicitor().setClaimTypeToSpecClaim();
        CaseData caseData = caseDataBuilder.build();

        assertFalse(awaitingResponsesFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void awaitingFirstDefendantResponsesNonFullDefenceReceivedShouldReturnTrue() {
        CaseDataBuilder caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimTwoDefendantSolicitors().setClaimTypeToSpecClaim();
        CaseData caseData = caseDataBuilder
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();

        assertTrue(awaitingResponsesNonFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void awaitingSecondDefendantResponsesNonFullDefenceReceivedShouldReturnTrue() {
        CaseDataBuilder caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimTwoDefendantSolicitors().setClaimTypeToSpecClaim();
        CaseData caseData = caseDataBuilder
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();

        assertTrue(awaitingResponsesNonFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void awaitingResponsesNonFullDefenceReceivedShouldReturnFalse() {
        CaseDataBuilder caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimTwoDefendantSolicitors().setClaimTypeToSpecClaim();
        CaseData caseData = caseDataBuilder.build();

        assertFalse(awaitingResponsesNonFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void awaitingResponsesNonFullDefenceReceivedShouldHitDefault() {
        CaseDataBuilder caseDataBuilder = CaseDataBuilder.builder().multiPartyClaimOneDefendantSolicitor().setClaimTypeToSpecClaim();
        CaseData caseData = caseDataBuilder.build();

        assertFalse(awaitingResponsesNonFullDefenceReceivedSpec.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtIssuedState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified_1v1().build();
        assertTrue(claimNotified.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataIsAtDraftState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
        assertFalse(claimNotified.test(caseData));
    }

    // 1v1 Case / 1v2 Same Solicitor (Field is null)
    @Test
    void shouldBeClaimNotified_whenSolicitorOptions_isNull() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimNotified_1v1()
            .build();

        assertTrue(claimNotified.test(caseData));
    }

    //1v2 - Notify Both Sol
    @Test
    void shouldBeClaimNotified_when1v2DifferentSolicitor_andNotifySolicitorOptions_isBoth() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimNotified_1v2_andNotifyBothSolicitors()
            .build();

        assertTrue(claimNotified.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataClaimDismissedPastClaimNotificationDeadline() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastClaimNotificationDeadline().build();
        assertTrue(pastClaimNotificationDeadline.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtStateAwaitingCaseNotification() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
        assertFalse(pastClaimNotificationDeadline.test(caseData));
    }

    @Test
    void shouldHandOffline_when1v2DifferentSolicitor_andNotifySolicitorOptions_isOneSolicitor() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor()
            .build();

        assertTrue(takenOfflineAfterClaimNotified.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterClaimIssue() {
        CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff().build();

        assertTrue(takenOfflineByStaffAfterClaimIssue.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterClaimIssueSpec() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateTakenOfflineByStaff()
            .setClaimNotificationDate()
            .setClaimTypeToSpecClaim().build();

        assertTrue(takenOfflineByStaffAfterClaimIssue.test(caseData));
    }

}
