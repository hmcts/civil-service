package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class DismissedPredicateTest {

    @Mock
    private CaseData caseData;

    @Nested
    class DismissedAfterClaimDetailNotified {

        @Test
        void should_return_true_for_dismissedAfterClaimDetailNotified_when_dismissal_deadline_passed_and_no_response_1v1() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
            when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
            when(caseData.getRespondent1ClaimResponseIntentionType()).thenReturn(null);
            when(caseData.getRespondent1ResponseDate()).thenReturn(null);
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);

            assertTrue(DismissedPredicate.dismissedAfterClaimDetailNotified.test(caseData));
        }

        @Test
        void should_return_true_for_dismissedAfterClaimDetailNotified_when_dismissal_deadline_passed_and_no_response_1v2() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
            when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
            when(caseData.getRespondent1ClaimResponseIntentionType()).thenReturn(null);
            when(caseData.getRespondent1ResponseDate()).thenReturn(null);
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
            // 1v2 scenario
            when(caseData.getRespondent2()).thenReturn(Party.builder().build());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
            when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(null);
            when(caseData.getRespondent2TimeExtensionDate()).thenReturn(null);
            when(caseData.getRespondent2ClaimResponseIntentionType()).thenReturn(null);
            when(caseData.getRespondent2ResponseDate()).thenReturn(null);

            assertTrue(DismissedPredicate.dismissedAfterClaimDetailNotified.test(caseData));
        }

        @Test
        void should_return_false_for_dismissedAfterClaimDetailNotified_when_dismissal_deadline_has_not_passed() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
            assertFalse(DismissedPredicate.dismissedAfterClaimDetailNotified.test(caseData));
        }

        @Test
        void should_return_false_for_dismissedAfterClaimDetailNotified_when_respondent1_has_acknowledged() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now().minusDays(2));
            assertFalse(DismissedPredicate.dismissedAfterClaimDetailNotified.test(caseData));
        }

        @Test
        void should_return_false_for_dismissedAfterClaimDetailNotified_when_case_taken_offline() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now().minusDays(2));
            assertFalse(DismissedPredicate.dismissedAfterClaimDetailNotified.test(caseData));
        }
    }

    @Nested
    class PastClaimDetailsNotificationDeadline {

        @Test
        void should_return_true_for_pastClaimDetailsNotificationDeadline_when_deadline_passed_and_not_notified() {
            when(caseData.getClaimDetailsNotificationDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getClaimDetailsNotificationDate()).thenReturn(null);
            when(caseData.getClaimNotificationDate()).thenReturn(LocalDateTime.now().minusDays(10));
            assertTrue(DismissedPredicate.pastClaimDetailsNotificationDeadline.test(caseData));
        }

        @Test
        void should_return_false_for_pastClaimDetailsNotificationDeadline_when_deadline_has_not_passed() {
            when(caseData.getClaimDetailsNotificationDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
            assertFalse(DismissedPredicate.pastClaimDetailsNotificationDeadline.test(caseData));
        }

        @Test
        void should_return_false_for_pastClaimDetailsNotificationDeadline_when_claim_details_notified() {
            when(caseData.getClaimDetailsNotificationDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getClaimDetailsNotificationDate()).thenReturn(LocalDateTime.now().minusDays(2));
            assertFalse(DismissedPredicate.pastClaimDetailsNotificationDeadline.test(caseData));
        }
    }

    @Nested
    class ClaimDismissedByCamunda {

        @Test
        void should_return_true_for_claimDismissedByCamunda_when_claim_dismissed_date_is_present() {
            when(caseData.getClaimDismissedDate()).thenReturn(LocalDateTime.now());
            assertTrue(DismissedPredicate.claimDismissedByCamunda.test(caseData));
        }

        @Test
        void should_return_false_for_claimDismissedByCamunda_when_claim_dismissed_date_is_not_present() {
            when(caseData.getClaimDismissedDate()).thenReturn(null);
            assertFalse(DismissedPredicate.claimDismissedByCamunda.test(caseData));
        }
    }

    @Nested
    class CaseDismissedPastHearingFeeDue {

        @Test
        void should_return_true_for_caseDismissedPastHearingFeeDue_when_hearing_fee_due_date_is_present() {
            when(caseData.getCaseDismissedHearingFeeDueDate()).thenReturn(LocalDateTime.now());
            assertTrue(DismissedPredicate.caseDismissedPastHearingFeeDue.test(caseData));
        }

        @Test
        void should_return_false_for_caseDismissedPastHearingFeeDue_when_hearing_fee_due_date_is_not_present() {
            when(caseData.getCaseDismissedHearingFeeDueDate()).thenReturn(null);
            assertFalse(DismissedPredicate.caseDismissedPastHearingFeeDue.test(caseData));
        }
    }
}
