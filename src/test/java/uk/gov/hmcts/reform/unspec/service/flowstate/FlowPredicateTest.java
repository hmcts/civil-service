package uk.gov.hmcts.reform.unspec.service.flowstate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.applicantOutOfTime;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.caseDismissed;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.caseDismissedAfterClaimAcknowledged;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.caseProceedsInCaseman;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimDetailsNotified;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimDiscontinued;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimIssued;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimNotified;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimTakenOffline;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.claimWithdrawn;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.failToNotifyClaim;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.fullDefenceProceed;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.pastClaimDetailsNotificationDeadline;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.paymentFailed;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.paymentSuccessful;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.pendingCaseIssued;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.respondent1NotRepresented;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.respondent1OrgNotRegistered;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.respondentAcknowledgeClaim;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.respondentCounterClaim;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.respondentFullAdmission;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.respondentFullDefence;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowPredicate.respondentPartAdmission;

class FlowPredicateTest {

    @Nested
    class ClaimIssuedPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseNotification().build();
            assertTrue(pendingCaseIssued.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            assertFalse(pendingCaseIssued.test(caseData));
        }
    }

    @Nested
    class ClaimNotifiedPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseDetailsNotification().build();
            assertTrue(claimNotified.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            assertFalse(claimNotified.test(caseData));
        }
    }

    @Nested
    class ClaimDetailsNotifiedPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated().build();
            assertTrue(claimDetailsNotified.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtAwaitingCaseDetailsNotification() {
            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseDetailsNotification().build();
            assertFalse(claimDetailsNotified.test(caseData));
        }
    }

    @Nested
    class Respondent1NotRepresented {

        @Test
        void shouldReturnTrue_whenRespondentNotRepresented() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendant().build();

            assertTrue(respondent1NotRepresented.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtAwaitingCaseNotificationState() {
            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseNotification().build();
            assertFalse(respondent1NotRepresented.test(caseData));
        }
    }

    @Nested
    class Respondent1NotRegistered {

        @Test
        void shouldReturnTrue_whenRespondentNotRegistered() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnregisteredDefendant().build();

            assertTrue(respondent1OrgNotRegistered.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtAwaitingCaseNotificationState() {
            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseNotification().build();
            assertFalse(respondent1OrgNotRegistered.test(caseData));
        }
    }

    @Nested
    class PaymentFailedPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentFailed().build();
            assertTrue(paymentFailed.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingCaseIssued().build();
            assertFalse(paymentFailed.test(caseData));
        }
    }

    @Nested
    class PaymentSuccessfulPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtIssuedState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();
            assertTrue(paymentSuccessful.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataIsAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingCaseIssued().build();
            assertFalse(paymentSuccessful.test(caseData));
        }
    }

    @Nested
    class CcdStateClaimIssuedPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataIsAtAwaitingCaseNotification() {
            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseNotification().build();
            assertTrue(claimIssued.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtDraftState() {
            CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();
            assertFalse(claimIssued.test(caseData));
        }
    }

    @Nested
    class RespondentAcknowledgedClaimPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimAcknowledge().build();
            assertTrue(respondentAcknowledgeClaim.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClaimCreated() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated().build();
            assertFalse(respondentAcknowledgeClaim.test(caseData));
        }
    }

    @Nested
    class RespondentFullDefencePredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullDefence() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
            assertTrue(respondentFullDefence.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClosed() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDiscontinued().build();
            assertFalse(respondentFullDefence.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateCaseProceedsInCaseman() {
            CaseData caseData = CaseDataBuilder.builder().atStateCaseProceedsInCaseman().build();
            assertFalse(respondentFullDefence.test(caseData));
        }
    }

    @Nested
    class RespondentFullAdmissionPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateFullAdmission() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmission().build();
            assertTrue(respondentFullAdmission.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateCaseProceedsInCaseman() {
            CaseData caseData = CaseDataBuilder.builder().atStateCaseProceedsInCaseman().build();
            assertFalse(respondentFullAdmission.test(caseData));
        }
    }

    @Nested
    class RespondentPartAdmissionPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtStatePartAdmission() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmission().build();
            assertTrue(respondentPartAdmission.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateCaseProceedsInCaseman() {
            CaseData caseData = CaseDataBuilder.builder().atStateCaseProceedsInCaseman().build();
            assertFalse(respondentPartAdmission.test(caseData));
        }
    }

    @Nested
    class RespondentCounterClaimPredicate {

        @Test
        void shouldReturnTrue_whenCaseDataAtStatePartAdmission() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentCounterClaim().build();
            assertTrue(respondentCounterClaim.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateCaseProceedsInCaseman() {
            CaseData caseData = CaseDataBuilder.builder().atStateCaseProceedsInCaseman().build();
            assertFalse(respondentCounterClaim.test(caseData));
        }
    }

    @Nested
    class ApplicantRespondToDefencePredicate {

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
    class WithdrawnClaimPredicate {

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class)
        void shouldReturnTrue_whenCaseDataAtStateClaimWithdrawn(FlowState.Main flowState) {
            CaseData caseData = CaseDataBuilder.builder().withdrawClaimFrom(flowState).build();
            assertTrue(claimWithdrawn.test(caseData));
        }

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class, mode = EnumSource.Mode.EXCLUDE, names = {"CLAIM_WITHDRAWN"})
        void shouldReturnFalse_whenCaseDataIsNotAtStateClaimWithdrawn(FlowState.Main flowState) {
            CaseData caseData = CaseDataBuilder.builder().atState(flowState).build();
            assertFalse(claimWithdrawn.test(caseData));
        }
    }

    @Nested
    class DiscontinuedClaimPredicate {

        @ParameterizedTest
        @EnumSource(value = FlowState.Main.class)
        void shouldReturnTrue_whenCaseDataAtStateClaimDiscontinued(FlowState.Main flowState) {
            CaseData caseData = CaseDataBuilder.builder().discontinueClaimFrom(flowState).build();
            assertTrue(claimDiscontinued.test(caseData));
        }

        @ParameterizedTest
        @EnumSource(
            value = FlowState.Main.class,
            mode = EnumSource.Mode.EXCLUDE,
            names = {
                "TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE",
                "CLAIM_DISCONTINUED"
            }
        )
        void shouldReturnFalse_whenCaseDataIsNotAtStateClaimDiscontinued(FlowState.Main flowState) {
            CaseData caseData = CaseDataBuilder.builder().atState(flowState).build();
            assertFalse(claimDiscontinued.test(caseData));
        }
    }

    @Nested
    class ClaimTakenOffline {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateProceedsOffline() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAdmissionOrCounterClaim().build();
            assertTrue(claimTakenOffline.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataNotAtStateProceedsOffline() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
            assertFalse(claimTakenOffline.test(caseData));
        }
    }

    @Nested
    class ClaimProceedsInCaseman {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateCaseProceedsInCaseman() {
            CaseData caseData = CaseDataBuilder.builder().atStateCaseProceedsInCaseman().build();
            assertTrue(caseProceedsInCaseman.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataNotAtStateProceedsOffline() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
            assertFalse(caseProceedsInCaseman.test(caseData));
        }
    }

    @Nested
    class ClaimDismissed {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissed() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissed().build();
            assertTrue(caseDismissed.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateApplicantRespondToDefence() {
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed().build();
            assertFalse(caseDismissed.test(caseData));
        }
    }

    @Nested
    class ClaimDismissedAfterClaimAcknowledged {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimAcknowledgeWithClaimDismissedDate() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimAcknowledge()
                .claimDismissedDate(LocalDateTime.now())
                .build();
            assertTrue(caseDismissedAfterClaimAcknowledged.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateClaimAcknowledge() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimAcknowledge().build();
            assertFalse(caseDismissedAfterClaimAcknowledged.test(caseData));
        }
    }

    @Nested
    class ApplicantOutOfTime {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateServiceAcknowledgeWithClaimDismissedDate() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflinePastApplicantResponseDeadline().build();
            assertTrue(applicantOutOfTime.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateServiceAcknowledge() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
            assertFalse(applicantOutOfTime.test(caseData));
        }
    }

    @Nested
    class FailToNotifyClaim {

        @Test
        void shouldReturnTrue_whenCaseDataClaimDismissedPastClaimNotificationDeadline() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissedPastClaimNotificationDeadline().build();
            assertTrue(failToNotifyClaim.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateAwaitingCaseNotification() {
            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseNotification().build();
            assertFalse(failToNotifyClaim.test(caseData));
        }
    }

    @Nested
    class PastClaimDetailsNotificationDeadline {

        @Test
        void shouldReturnTrue_whenCaseDataAtStateClaimDismissedPastClaimDetailsNotificationDeadline() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDismissedPastClaimDetailsNotificationDeadline()
                .build();
            assertTrue(pastClaimDetailsNotificationDeadline.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenCaseDataAtStateAwaitingCaseDetailsNotification() {
            CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseDetailsNotification().build();
            assertFalse(pastClaimDetailsNotificationDeadline.test(caseData));
        }
    }
}
