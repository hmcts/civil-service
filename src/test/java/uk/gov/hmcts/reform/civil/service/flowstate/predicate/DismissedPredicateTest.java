package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

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
            assertTrue(DismissedPredicate.afterClaimDetailNotified.test(caseData));
        }

        @Test
        void should_return_true_for_dismissedAfterClaimDetailNotified_when_dismissal_deadline_passed_and_no_response_1v2() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
            when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
            when(caseData.getRespondent1ClaimResponseIntentionType()).thenReturn(null);
            when(caseData.getRespondent1ResponseDate()).thenReturn(null);
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
            when(caseData.getRespondent2()).thenReturn(Party.builder().build());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
            when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(null);
            when(caseData.getRespondent2TimeExtensionDate()).thenReturn(null);
            when(caseData.getRespondent2ClaimResponseIntentionType()).thenReturn(null);
            when(caseData.getRespondent2ResponseDate()).thenReturn(null);
            assertTrue(DismissedPredicate.afterClaimDetailNotified.test(caseData));
        }

        @Test
        void should_return_false_for_dismissedAfterClaimDetailNotified_when_dismissal_deadline_has_not_passed() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
            assertFalse(DismissedPredicate.afterClaimDetailNotified.test(caseData));
        }

        @Test
        void should_return_false_for_dismissedAfterClaimDetailNotified_when_respondent1_has_acknowledged() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now().minusDays(2));
            assertFalse(DismissedPredicate.afterClaimDetailNotified.test(caseData));
        }

        @Test
        void should_return_false_for_dismissedAfterClaimDetailNotified_when_case_taken_offline() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now().minusDays(2));
            assertFalse(DismissedPredicate.afterClaimDetailNotified.test(caseData));
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
            assertTrue(DismissedPredicate.byCamunda.test(caseData));
        }

        @Test
        void should_return_false_for_claimDismissedByCamunda_when_claim_dismissed_date_is_not_present() {
            when(caseData.getClaimDismissedDate()).thenReturn(null);
            assertFalse(DismissedPredicate.byCamunda.test(caseData));
        }
    }

    @Nested
    class CaseDismissedPastHearingFeeDue {

        @Test
        void should_return_true_for_caseDismissedPastHearingFeeDue_when_hearing_fee_due_date_is_present() {
            when(caseData.getCaseDismissedHearingFeeDueDate()).thenReturn(LocalDateTime.now());
            assertTrue(DismissedPredicate.pastHearingFeeDue.test(caseData));
        }

        @Test
        void should_return_false_for_caseDismissedPastHearingFeeDue_when_hearing_fee_due_date_is_not_present() {
            when(caseData.getCaseDismissedHearingFeeDueDate()).thenReturn(null);
            assertFalse(DismissedPredicate.pastHearingFeeDue.test(caseData));
        }
    }

    @Nested
    class PastClaimDeadlinePredicate {

        @Test
        void should_return_true_when_dismissal_deadline_has_passed() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            assertTrue(DismissedPredicate.pastClaimDeadline.test(caseData));
        }

        @Test
        void should_return_false_when_no_dismissal_deadline() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(null);
            assertFalse(DismissedPredicate.pastClaimDeadline.test(caseData));
        }

        @Test
        void should_return_false_when_dismissal_deadline_in_future() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
            assertFalse(DismissedPredicate.pastClaimDeadline.test(caseData));
        }
    }

    @Nested
    class PastClaimNotificationDeadlinePredicate {

        @Test
        void should_return_true_when_claim_notification_deadline_passed_and_no_notification_date() {
            when(caseData.getClaimNotificationDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getClaimNotificationDate()).thenReturn(null);
            assertTrue(DismissedPredicate.pastClaimNotificationDeadline.test(caseData));
        }

        @Test
        void should_return_false_when_notification_deadline_not_passed() {
            when(caseData.getClaimNotificationDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
            assertFalse(DismissedPredicate.pastClaimNotificationDeadline.test(caseData));
        }

        @Test
        void should_return_false_when_notification_date_present_even_if_deadline_passed() {
            when(caseData.getClaimNotificationDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getClaimNotificationDate()).thenReturn(LocalDateTime.now().minusDays(2));
            assertFalse(DismissedPredicate.pastClaimNotificationDeadline.test(caseData));
        }
    }

    @Nested
    class AfterClaimNotifiedExtensionPredicate {

        @Test
        void should_return_true_when_deadline_passed_and_r1_extension_without_ack_and_no_intention_from_both() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent1ClaimResponseIntentionType()).thenReturn(null);
            when(caseData.getRespondent2ClaimResponseIntentionType()).thenReturn(null);
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
            when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now().minusDays(3));
            assertTrue(DismissedPredicate.afterClaimNotifiedExtension.test(caseData));
        }

        @Test
        void should_return_true_when_deadline_passed_and_r2_extension_without_ack_and_no_intention_from_both() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent1ClaimResponseIntentionType()).thenReturn(null);
            when(caseData.getRespondent2ClaimResponseIntentionType()).thenReturn(null);
            when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(null);
            when(caseData.getRespondent2TimeExtensionDate()).thenReturn(LocalDateTime.now().minusDays(3));
            assertTrue(DismissedPredicate.afterClaimNotifiedExtension.test(caseData));
        }

        @Test
        void should_return_false_when_deadline_not_passed() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
            assertFalse(DismissedPredicate.afterClaimNotifiedExtension.test(caseData));
        }

        @Test
        void should_return_false_when_both_have_acknowledged_even_with_time_extensions() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent1ClaimResponseIntentionType()).thenReturn(null);
            when(caseData.getRespondent2ClaimResponseIntentionType()).thenReturn(null);
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now().minusDays(2));
            when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now().minusDays(2));
            assertFalse(DismissedPredicate.afterClaimNotifiedExtension.test(caseData));
        }

        @Test
        void should_return_false_when_no_time_extension_or_acknowledged() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent1ClaimResponseIntentionType()).thenReturn(null);
            when(caseData.getRespondent2ClaimResponseIntentionType()).thenReturn(null);
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent2TimeExtensionDate()).thenReturn(null);
            assertFalse(DismissedPredicate.afterClaimNotifiedExtension.test(caseData));
        }
    }

    @Nested
    class AfterClaimAcknowledgedPredicate {

        @Test
        void should_return_true_for_1v1_when_deadline_passed_r1_acked_no_extension_not_offline_and_no_r1_response() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now().minusDays(2));
            when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
            when(caseData.getRespondent1ResponseDate()).thenReturn(null);
            assertTrue(DismissedPredicate.afterClaimAcknowledged.test(caseData));
        }

        @Test
        void should_return_true_for_1v2_twoLR_when_both_acked_no_extensions_and_at_least_one_no_response() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent2()).thenReturn(Party.builder().build());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO);
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now().minusDays(3));
            when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now().minusDays(3));
            when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
            when(caseData.getRespondent2TimeExtensionDate()).thenReturn(null);
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
            assertTrue(DismissedPredicate.afterClaimAcknowledged.test(caseData));
        }

        @Test
        void should_return_false_when_taken_offline_by_staff() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
            assertFalse(DismissedPredicate.afterClaimAcknowledged.test(caseData));
        }

        @Test
        void should_return_false_when_r1_has_time_extension() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now());
            assertFalse(DismissedPredicate.afterClaimAcknowledged.test(caseData));
        }

        @Test
        void should_return_false_when_r1_not_acknowledged() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
            assertFalse(DismissedPredicate.afterClaimAcknowledged.test(caseData));
        }

        @Test
        void should_return_false_for_twoLR_when_both_responded() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent2()).thenReturn(Party.builder().build());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO);
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now().minusDays(2));
            when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now().minusDays(2));
            when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
            when(caseData.getRespondent2TimeExtensionDate()).thenReturn(null);
            when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent2ResponseDate()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
            assertFalse(DismissedPredicate.afterClaimAcknowledged.test(caseData));
        }

        @Test
        void should_return_false_when_deadline_not_passed() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
            assertFalse(DismissedPredicate.afterClaimAcknowledged.test(caseData));
        }
    }

    @Nested
    class AfterClaimAcknowledgedExtensionPredicate {

        @Test
        void should_return_true_for_1v1_when_deadline_passed_r1_acked_with_time_extension_and_not_offline() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now().minusDays(2));
            when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getReasonNotSuitableSDO()).thenReturn(null);
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
            assertTrue(DismissedPredicate.afterClaimAcknowledgedExtension.test(caseData));
        }

        @Test
        void should_return_true_for_1v2_twoLR_when_both_acked_and_r2_has_time_extension() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent2()).thenReturn(Party.builder().build());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO);
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now().minusDays(3));
            when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now().minusDays(3));
            when(caseData.getRespondent2TimeExtensionDate()).thenReturn(LocalDateTime.now().minusDays(2));
            when(caseData.getReasonNotSuitableSDO()).thenReturn(null);
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
            assertTrue(DismissedPredicate.afterClaimAcknowledgedExtension.test(caseData));
        }

        @Test
        void should_return_true_for_1v2_oneLR_when_both_acked_and_r1_has_time_extension() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent2()).thenReturn(Party.builder().build());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now().minusDays(3));
            when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now().minusDays(3));
            when(caseData.getRespondent1TimeExtensionDate()).thenReturn(LocalDateTime.now().minusDays(2));
            when(caseData.getReasonNotSuitableSDO()).thenReturn(null);
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
            assertTrue(DismissedPredicate.afterClaimAcknowledgedExtension.test(caseData));
        }

        @Test
        void should_return_false_when_taken_offline_by_staff() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
            assertFalse(DismissedPredicate.afterClaimAcknowledgedExtension.test(caseData));
        }

        @Test
        void should_return_false_when_deadline_not_passed() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
            assertFalse(DismissedPredicate.afterClaimAcknowledgedExtension.test(caseData));
        }

        @Test
        void should_return_false_for_1v2_when_no_time_extension_on_either() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent2()).thenReturn(Party.builder().build());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO);
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now().minusDays(3));
            when(caseData.getRespondent2AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now().minusDays(3));
            when(caseData.getReasonNotSuitableSDO()).thenReturn(null);
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
            assertFalse(DismissedPredicate.afterClaimAcknowledgedExtension.test(caseData));
        }

        @Test
        void should_return_false_for_1v1_when_r1_has_no_time_extension() {
            when(caseData.getClaimDismissedDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1TimeExtensionDate()).thenReturn(null);
            when(caseData.getReasonNotSuitableSDO()).thenReturn(null);
            when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
            assertFalse(DismissedPredicate.afterClaimAcknowledgedExtension.test(caseData));
        }
    }
}
