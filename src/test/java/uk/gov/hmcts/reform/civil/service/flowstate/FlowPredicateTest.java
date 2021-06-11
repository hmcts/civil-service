package uk.gov.hmcts.reform.civil.service.flowstate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.applicantOutOfTime;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.applicantOutOfTimeProcessedByCamunda;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterClaimAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterClaimAcknowledgedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterDetailNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterDetailNotifiedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDetailsNotifiedTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDismissedByCamunda;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedOneRespondentRepresentative;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimSubmittedTwoRespondentRepresentatives;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaim;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaimAfterAcknowledge;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.counterClaimAfterNotifyDetails;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmission;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmissionAfterAcknowledge;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullAdmissionAfterNotifyDetails;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefence;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceAfterAcknowledge;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceAfterNotifyDetails;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.fullDefenceProceed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.notificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.notificationAcknowledgedTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmission;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmissionAfterAcknowledge;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.partAdmissionAfterNotifyDetails;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pastClaimDetailsNotificationDeadline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pastClaimNotificationDeadline;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentFailed;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.paymentSuccessful;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.pendingClaimIssued;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent1NotRepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent1OrgNotRegistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaff;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimDetailsNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimDetailsNotifiedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimIssue;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterClaimNotified;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterNotificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension;

class FlowPredicateTest {

    @Nested
    class ClaimSubmittedOneRespondentRepresentative {

        @Test
        void shouldReturnTrue_whenCaseDataAtClaimSubmittedState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            assertTrue(claimSubmittedOneRespondentRepresentative.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtClaimSubmittedOneRespondentRepresentativeState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedOneRespondentRepresentative().build();
            assertTrue(claimSubmittedOneRespondentRepresentative.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives().build();
            assertFalse(claimSubmittedOneRespondentRepresentative.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            assertFalse(claimSubmittedOneRespondentRepresentative.test(caseData));
        }
    }

    @Nested
    class ClaimSubmittedTwoRespondentRepresentatives {

        @Test
        void shouldReturnTrue_whenCaseDataAtClaimSubmittedTwoRespondentRepresentativesState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedTwoRespondentRepresentatives().build();
            assertTrue(claimSubmittedTwoRespondentRepresentatives.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            assertFalse(claimSubmittedTwoRespondentRepresentatives.test(caseData));
        }
    }

    @Nested
    class ClaimNotified {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified().build();
            assertTrue(claimNotified.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            assertFalse(claimNotified.test(caseData));
        }
    }

    @Nested
    class ClaimDetailsNotified {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            assertTrue(claimDetailsNotified.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtAwaitingCaseDetailsNotification() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified().build();
            assertFalse(claimDetailsNotified.test(caseData));
        }
    }

    @Nested
    class ClaimDetailsNotifiedTimeExtension {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension().build();
            assertTrue(claimDetailsNotifiedTimeExtension.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtAwaitingCaseDetailsNotification() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified().build();
            assertFalse(claimDetailsNotifiedTimeExtension.test(caseData));
        }
    }

    @Nested
    class Respondent1NotRepresented {

        @Test
        void shouldReturnTrue_whenRespondentNotRepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnRepresentedDefendant().build();
            assertTrue(respondent1NotRepresented.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtAwaitingCaseNotificationState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            assertFalse(respondent1NotRepresented.test(caseData));
        }
    }

    @Nested
    class Respondent1NotRegistered {

        @Test
        void shouldReturnTrue_whenRespondentNotRegistered() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnRegisteredDefendant().build();
            assertTrue(respondent1OrgNotRegistered.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtAwaitingCaseNotificationState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            assertFalse(respondent1OrgNotRegistered.test(caseData));
        }
    }

    @Nested
    class PaymentFailed {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentFailed().build();
            assertTrue(paymentFailed.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            assertFalse(paymentFailed.test(caseData));
        }
    }

    @Nested
    class PaymentSuccessful {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();
            assertTrue(paymentSuccessful.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
            assertFalse(paymentSuccessful.test(caseData));
        }
    }

    @Nested
    class PendingClaimIssued {

        @Test
        void shouldReturnTrue_whenCaseDataIsAtPendingClaimIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            assertTrue(pendingClaimIssued.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();
            assertFalse(pendingClaimIssued.test(caseData));
        }
    }

    @Nested
    class ClaimIssued {

        @Test
        void shouldReturnTrue_whenCaseDataIsAtClaimIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            assertTrue(claimIssued.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();
            assertFalse(claimIssued.test(caseData));
        }
    }

    @Nested
    class NotificationAcknowledged {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            assertTrue(notificationAcknowledged.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            assertFalse(notificationAcknowledged.test(caseData));
        }
    }

    @Nested
    class NotificationAcknowledgedTimeExtension {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedTimeExtension().build();
            assertTrue(notificationAcknowledgedTimeExtension.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            assertFalse(notificationAcknowledgedTimeExtension.test(caseData));
        }
    }

    @Nested
    class RespondentFullDefence {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefence() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
            assertTrue(fullDefence.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefenceAfterNotifyDetails() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefenceAfterNotifyDetails().build();
            assertTrue(fullDefenceAfterNotifyDetails.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefenceAfterNotificationAcknowledgement() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
            assertTrue(fullDefenceAfterAcknowledge.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClosed() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDiscontinued().build();
            assertFalse(fullDefence.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateCaseProceedsInCaseman() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff().build();
            assertFalse(fullDefence.test(caseData));
        }
    }

    @Nested
    class RespondentFullAdmission {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullAdmission() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmission().build();
            assertTrue(fullAdmission.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullAdmissionAfterNotifyDetails() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionAfterNotifyDetails().build();
            assertTrue(fullAdmissionAfterNotifyDetails.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullAdmissionAfterAcknowledge() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmission().build();
            assertTrue(fullAdmissionAfterAcknowledge.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateCaseProceedsInCaseman() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff().build();
            assertFalse(fullAdmission.test(caseData));
        }
    }

    @Nested
    class RespondentPartAdmission {

        @Test
        void shouldReturnTrue_whenCaseDataAtStatePartAdmission() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmission().build();
            assertTrue(partAdmission.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStatePartAdmissionAfterNotifyDetails() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmissionAfterNotifyDetails().build();
            assertTrue(partAdmissionAfterNotifyDetails.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStatePartAdmissionAfterAcknowledge() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmission().build();
            assertTrue(partAdmissionAfterAcknowledge.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateCaseProceedsInCaseman() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff().build();
            assertFalse(partAdmission.test(caseData));
        }
    }

    @Nested
    class RespondentCounterClaim {

        @Test
        void shouldReturnTrue_whenCaseDataAtStatePartAdmission() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentCounterClaim().build();
            assertTrue(counterClaim.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStatePartAdmissionAfterNotifyDetails() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentCounterClaimAfterNotifyDetails().build();
            assertTrue(counterClaimAfterNotifyDetails.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStatePartAdmissionAfterAcknowledge() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentCounterClaim().build();
            assertTrue(counterClaimAfterAcknowledge.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateCaseProceedsInCaseman() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff().build();
            assertFalse(counterClaim.test(caseData));
        }
    }

    @Nested
    class ApplicantRespondToDefence {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefence() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
            assertTrue(fullDefenceProceed.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClaimAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
            assertFalse(fullDefenceProceed.test(caseData));
        }
    }

    @Nested
    class ClaimTakenOfflineByStaff {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterClaimIssue() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff().build();

            assertTrue(takenOfflineByStaffAfterClaimIssue.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterClaimNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterClaimNotified().build();
            assertTrue(takenOfflineByStaffAfterClaimNotified.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterClaimDetailsNotified().build();
            assertTrue(takenOfflineByStaffAfterClaimDetailsNotified.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterClaimDetailsNotifiedExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterClaimDetailsNotifiedExtension()
                .build();
            assertTrue(takenOfflineByStaffAfterClaimDetailsNotifiedExtension.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterNotificationAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterNotificationAcknowledged()
                .build();
            assertTrue(takenOfflineByStaffAfterNotificationAcknowledged.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterNotificationAcknowledgedExtnesion() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension().build();
            assertTrue(takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflineAfterDefendantResponse() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterDefendantResponse().build();
            assertTrue(takenOfflineByStaff.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataNotAtStateProceedsOffline() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
            assertFalse(takenOfflineByStaff.test(caseData));
        }
    }

    @Nested
    class ClaimDismissed {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissed().build();
            assertTrue(caseDismissedAfterDetailNotified.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterClaimNotifiedExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            assertTrue(caseDismissedAfterDetailNotifiedExtension.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterNotificationAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            assertTrue(caseDismissedAfterClaimAcknowledged.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedAfterNotificationAcknowledgedExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedTimeExtension()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            assertTrue(caseDismissedAfterClaimAcknowledgedExtension.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateApplicantRespondToDefence() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .claimDismissedDeadline(LocalDateTime.now().minusDays(5))
                .build();
            assertFalse(caseDismissedAfterDetailNotified.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClaimAcknowledge() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            assertFalse(caseDismissedAfterClaimAcknowledged.test(caseData));
        }
    }

    @Nested
    class ApplicantOutOfTime {

        @Test
        void shouldReturnTrue_whenCaseDataIsAtStatePastApplicantResponseDeadline() {
            CaseData caseData = CaseDataBuilder.builder().atStatePastApplicantResponseDeadline().build();
            assertTrue(applicantOutOfTime.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtStateApplicantRespondToDefenceAndProceed() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
            assertFalse(applicantOutOfTime.test(caseData));
        }
    }

    @Nested
    class ApplicantOutOfTimeProcessedByCamunda {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateTakenOfflinePastApplicantResponseDeadline() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflinePastApplicantResponseDeadline().build();
            assertTrue(applicantOutOfTimeProcessedByCamunda.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateApplicantRespondToDefenceAndProceed() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
            assertFalse(applicantOutOfTimeProcessedByCamunda.test(caseData));
        }
    }

    @Nested
    class FailToNotifyClaim {

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
    }

    @Nested
    class PastClaimDetailsNotificationDeadline {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedPastClaimDetailsNotificationDeadline() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimPastClaimDetailsNotificationDeadline()
                .build();
            assertTrue(pastClaimDetailsNotificationDeadline.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateAwaitingCaseDetailsNotification() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimNotified().build();
            assertFalse(pastClaimDetailsNotificationDeadline.test(caseData));
        }
    }

    @Nested
    class DismissedByCamunda {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedPastClaimDetailsNotificationDeadline() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDismissedPastClaimDetailsNotificationDeadline()
                .build();
            assertTrue(claimDismissedByCamunda.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClaimPastClaimDetailsNotificationDeadline() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimPastClaimDetailsNotificationDeadline().build();
            assertFalse(claimDismissedByCamunda.test(caseData));
        }
    }
}
