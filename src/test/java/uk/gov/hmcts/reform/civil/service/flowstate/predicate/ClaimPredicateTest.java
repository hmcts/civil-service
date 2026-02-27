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
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class ClaimPredicateTest {

    @Mock
    private CaseData caseData;

    @Nested
    class IssuedPredicate {

        @Test
        void should_return_true_for_issued_when_claim_has_notification_deadline() {
            when(caseData.getClaimNotificationDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
            assertTrue(ClaimPredicate.issued.test(caseData));
        }

        @Test
        void should_return_false_for_issued_when_claim_does_not_have_notification_deadline() {
            when(caseData.getClaimNotificationDeadline()).thenReturn(null);
            assertFalse(ClaimPredicate.issued.test(caseData));
        }
    }

    @Nested
    class Submitted1v1RespondentOneUnregisteredPredicate {

        @Test
        void should_return_true_when_submitted_1v1_r1_represented_and_unregistered() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(NO);
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent1OrgRegistered()).thenReturn(NO);
            assertTrue(ClaimPredicate.submitted1v1RespondentOneUnregistered.test(caseData));
        }

        @Test
        void should_return_false_when_no_submitted_date() {
            when(caseData.getSubmittedDate()).thenReturn(null);
            assertFalse(ClaimPredicate.submitted1v1RespondentOneUnregistered.test(caseData));
        }

        @Test
        void should_return_false_when_addRespondent2_is_yes() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(YES);
            assertFalse(ClaimPredicate.submitted1v1RespondentOneUnregistered.test(caseData));
        }

        @Test
        void should_return_false_when_addRespondent2_is_null() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            assertFalse(ClaimPredicate.submitted1v1RespondentOneUnregistered.test(caseData));
        }

        @Test
        void should_return_false_when_r1_unrepresented() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(NO);
            when(caseData.getRespondent1Represented()).thenReturn(NO);
            assertFalse(ClaimPredicate.submitted1v1RespondentOneUnregistered.test(caseData));
        }

        @Test
        void should_return_false_when_r1_org_registered_yes() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(NO);
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent1OrgRegistered()).thenReturn(YES);
            assertFalse(ClaimPredicate.submitted1v1RespondentOneUnregistered.test(caseData));
        }
    }

    @Nested
    class SubmittedOneUnrepresentedDefendantOnlyPredicate {

        @Test
        void should_return_true_when_submitted_and_r1_unrepresented_and_addRespondent2_null() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1Represented()).thenReturn(NO);
            assertTrue(ClaimPredicate.submittedOneUnrepresentedDefendantOnly.test(caseData));
        }

        @Test
        void should_return_true_when_submitted_and_r1_unrepresented_and_addRespondent2_no() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1Represented()).thenReturn(NO);
            when(caseData.getAddRespondent2()).thenReturn(NO);
            assertTrue(ClaimPredicate.submittedOneUnrepresentedDefendantOnly.test(caseData));
        }

        @Test
        void should_return_false_when_no_submitted_date() {
            when(caseData.getSubmittedDate()).thenReturn(null);
            assertFalse(ClaimPredicate.submittedOneUnrepresentedDefendantOnly.test(caseData));
        }

        @Test
        void should_return_false_when_r1_represented() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            assertFalse(ClaimPredicate.submittedOneUnrepresentedDefendantOnly.test(caseData));
        }

        @Test
        void should_return_false_when_addRespondent2_yes() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1Represented()).thenReturn(NO);
            when(caseData.getAddRespondent2()).thenReturn(YES);
            assertFalse(ClaimPredicate.submittedOneUnrepresentedDefendantOnly.test(caseData));
        }
    }

    @Nested
    class SubmittedRespondent1UnrepresentedPredicate {

        @Test
        void should_return_true_when_submitted_and_r1_unrepresented() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1Represented()).thenReturn(NO);
            assertTrue(ClaimPredicate.submittedRespondent1Unrepresented.test(caseData));
        }

        @Test
        void should_return_false_when_no_submitted_date() {
            when(caseData.getSubmittedDate()).thenReturn(null);
            assertFalse(ClaimPredicate.submittedRespondent1Unrepresented.test(caseData));
        }

        @Test
        void should_return_false_when_r1_represented() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            assertFalse(ClaimPredicate.submittedRespondent1Unrepresented.test(caseData));
        }
    }

    @Nested
    class SubmittedRespondent2UnrepresentedPredicate {

        @Test
        void should_return_true_when_submitted_addRespondent2_yes_and_r2_unrepresented() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2Represented()).thenReturn(NO);
            assertTrue(ClaimPredicate.submittedRespondent2Unrepresented.test(caseData));
        }

        @Test
        void should_return_false_when_no_submitted_date() {
            when(caseData.getSubmittedDate()).thenReturn(null);
            assertFalse(ClaimPredicate.submittedRespondent2Unrepresented.test(caseData));
        }

        @Test
        void should_return_false_when_addRespondent2_not_yes() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(NO);
            assertFalse(ClaimPredicate.submittedRespondent2Unrepresented.test(caseData));
        }

        @Test
        void should_return_false_when_r2_represented() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2Represented()).thenReturn(YES);
            assertFalse(ClaimPredicate.submittedRespondent2Unrepresented.test(caseData));
        }
    }

    @Nested
    class PendingIssuedPredicate {

        @Test
        void should_return_true_for_1v1_path_no_respondent2() {
            when(caseData.getIssueDate()).thenReturn(java.time.LocalDate.now());
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent1OrgRegistered()).thenReturn(YES);
            assertTrue(ClaimPredicate.pendingIssued.test(caseData));
        }

        @Test
        void should_return_true_for_1v2_r2_represented_and_org_registered() {
            when(caseData.getIssueDate()).thenReturn(java.time.LocalDate.now());
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent1OrgRegistered()).thenReturn(YES);
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2Represented()).thenReturn(YES);
            when(caseData.getRespondent2OrgRegistered()).thenReturn(YES);
            assertTrue(ClaimPredicate.pendingIssued.test(caseData));
        }

        @Test
        void should_return_true_for_1v2_r2_represented_and_same_legal_rep_yes() {
            when(caseData.getIssueDate()).thenReturn(java.time.LocalDate.now());
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent1OrgRegistered()).thenReturn(YES);
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2Represented()).thenReturn(YES);
            when(caseData.getRespondent2OrgRegistered()).thenReturn(NO);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
            assertTrue(ClaimPredicate.pendingIssued.test(caseData));
        }

        @Test
        void should_return_false_when_no_issue_date() {
            when(caseData.getIssueDate()).thenReturn(null);
            assertFalse(ClaimPredicate.pendingIssued.test(caseData));
        }

        @Test
        void should_return_false_when_r1_unrepresented() {
            when(caseData.getIssueDate()).thenReturn(java.time.LocalDate.now());
            when(caseData.getRespondent1Represented()).thenReturn(NO);

            assertFalse(ClaimPredicate.pendingIssued.test(caseData));
        }

        @Test
        void should_return_false_when_r1_org_not_registered() {
            when(caseData.getIssueDate()).thenReturn(java.time.LocalDate.now());
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent1OrgRegistered()).thenReturn(NO);
            assertFalse(ClaimPredicate.pendingIssued.test(caseData));
        }

        @Test
        void should_return_false_when_r2_present_but_unrepresented() {
            when(caseData.getIssueDate()).thenReturn(java.time.LocalDate.now());
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent1OrgRegistered()).thenReturn(YES);
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2Represented()).thenReturn(NO);
            assertFalse(ClaimPredicate.pendingIssued.test(caseData));
        }

        @Test
        void should_return_false_when_r2_represented_but_org_not_registered_and_not_same_legal_rep() {
            when(caseData.getIssueDate()).thenReturn(java.time.LocalDate.now());
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent1OrgRegistered()).thenReturn(YES);
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2Represented()).thenReturn(YES);
            when(caseData.getRespondent2OrgRegistered()).thenReturn(NO);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            assertFalse(ClaimPredicate.pendingIssued.test(caseData));
        }
    }

    @Nested
    class PendingIssuedUnrepresentedPredicate {

        @Test
        void should_return_true_for_non_spec_both_unrepresented() {
            when(caseData.getIssueDate()).thenReturn(java.time.LocalDate.now());
            when(caseData.getRespondent1Represented()).thenReturn(NO);
            when(caseData.getRespondent2Represented()).thenReturn(NO);
            assertTrue(ClaimPredicate.pendingIssuedUnrepresented.test(caseData));
        }

        @Test
        void should_return_true_for_spec_multiparty_both_unrepresented() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent1Represented()).thenReturn(NO);
            when(caseData.getRespondent2Represented()).thenReturn(NO);
            assertTrue(ClaimPredicate.pendingIssuedUnrepresented.test(caseData));
        }

        @Test
        void should_return_false_for_non_spec_when_both_represented() {
            when(caseData.getIssueDate()).thenReturn(java.time.LocalDate.now());
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent2Represented()).thenReturn(YES);
            assertFalse(ClaimPredicate.pendingIssuedUnrepresented.test(caseData));
        }

        @Test
        void should_return_false_for_spec_single_party_even_if_unrepresented() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getRespondent1Represented()).thenReturn(NO);
            when(caseData.getRespondent2Represented()).thenReturn(NO);
            assertFalse(ClaimPredicate.pendingIssuedUnrepresented.test(caseData));
        }
    }

    @Nested
    class AfterIssuedPredicate {

        @Test
        void should_return_true_for_spec_with_notification_date_and_future_deadline_and_no_ack_or_response() {
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
            when(caseData.getRespondent1ResponseDate()).thenReturn(null);
            when(caseData.getClaimNotificationDeadline()).thenReturn(LocalDateTime.now().plusDays(2));
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getClaimNotificationDate()).thenReturn(java.time.LocalDateTime.now());
            assertTrue(ClaimPredicate.afterIssued.test(caseData));
        }

        @Test
        void should_return_true_for_unspec_without_notification_date_and_future_deadline_and_no_ack_or_response() {
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
            when(caseData.getRespondent1ResponseDate()).thenReturn(null);
            when(caseData.getClaimNotificationDeadline()).thenReturn(LocalDateTime.now().plusDays(2));
            when(caseData.getClaimNotificationDate()).thenReturn(null);
            assertTrue(ClaimPredicate.afterIssued.test(caseData));
        }

        @Test
        void should_return_false_when_claim_details_notification_date_present() {
            when(caseData.getClaimDetailsNotificationDate()).thenReturn(java.time.LocalDateTime.now());
            assertFalse(ClaimPredicate.afterIssued.test(caseData));
        }

        @Test
        void should_return_false_when_acknowledgement_present() {
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(LocalDateTime.now());

            assertFalse(ClaimPredicate.afterIssued.test(caseData));
        }

        @Test
        void should_return_false_when_response_present() {
            when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
            assertFalse(ClaimPredicate.afterIssued.test(caseData));
        }

        @Test
        void should_return_false_when_deadline_not_in_future() {
            when(caseData.getClaimNotificationDeadline()).thenReturn(LocalDateTime.now().minusDays(1));
            assertFalse(ClaimPredicate.afterIssued.test(caseData));
        }

        @Test
        void should_return_false_when_spec_without_notification_date() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getClaimNotificationDate()).thenReturn(null);
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
            when(caseData.getRespondent1ResponseDate()).thenReturn(null);
            when(caseData.getClaimNotificationDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
            assertFalse(ClaimPredicate.afterIssued.test(caseData));
        }

        @Test
        void should_return_false_when_unspec_with_notification_date_present() {
            when(caseData.getCaseAccessCategory()).thenReturn(null);
            when(caseData.getClaimNotificationDate()).thenReturn(java.time.LocalDateTime.now());
            when(caseData.getRespondent1AcknowledgeNotificationDate()).thenReturn(null);
            when(caseData.getRespondent1ResponseDate()).thenReturn(null);
            when(caseData.getClaimNotificationDeadline()).thenReturn(LocalDateTime.now().plusDays(1));
            assertFalse(ClaimPredicate.afterIssued.test(caseData));
        }
    }

    @Nested
    class IsSpecPredicate {

        @Test
        void should_return_true_for_isSpec_when_claim_is_spec_claim() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            assertTrue(ClaimPredicate.isSpec.test(caseData));
        }

        @Test
        void should_return_false_for_isSpec_when_claim_is_not_spec_claim() {
            when(caseData.getCaseAccessCategory()).thenReturn(null);
            assertFalse(ClaimPredicate.isSpec.test(caseData));
        }
    }

    @Nested
    class SubmittedOneRespondentRepresentativePredicate {

        @Test
        void should_return_true_when_submitted_and_no_second_respondent_field_and_resp1_represented() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            assertTrue(ClaimPredicate.submittedOneRespondentRepresentative.test(caseData));
        }

        @Test
        void should_return_true_when_submitted_and_addRespondent2_NO_and_resp1_represented() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getAddRespondent2()).thenReturn(NO);
            assertTrue(ClaimPredicate.submittedOneRespondentRepresentative.test(caseData));
        }

        @Test
        void should_return_true_when_submitted_and_addRespondent2_YES_with_same_legal_representative() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
            assertTrue(ClaimPredicate.submittedOneRespondentRepresentative.test(caseData));
        }

        @Test
        void should_return_false_when_submitted_and_addRespondent2_YES_with_different_legal_representatives() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            assertFalse(ClaimPredicate.submittedOneRespondentRepresentative.test(caseData));
        }

        @Test
        void should_return_false_when_no_submitted_date() {
            when(caseData.getSubmittedDate()).thenReturn(null);
            assertFalse(ClaimPredicate.submittedOneRespondentRepresentative.test(caseData));
        }

        @Test
        void should_return_false_when_respondent1_unrepresented_even_if_submitted() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1Represented()).thenReturn(NO);
            assertFalse(ClaimPredicate.submittedOneRespondentRepresentative.test(caseData));
        }
    }

    @Nested
    class SubmittedTwoRegisteredRespondentRepresentativesPredicate {

        @Test
        void should_return_true_when_all_conditions_met() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent2Represented()).thenReturn(YES);
            when(caseData.getRespondent1OrgRegistered()).thenReturn(YES);
            when(caseData.getRespondent2OrgRegistered()).thenReturn(YES);
            assertTrue(ClaimPredicate.submittedTwoRegisteredRespondentRepresentatives.test(caseData));
        }

        @Test
        void should_return_false_when_no_submitted_date() {
            when(caseData.getSubmittedDate()).thenReturn(null);
            assertFalse(ClaimPredicate.submittedTwoRegisteredRespondentRepresentatives.test(caseData));
        }

        @Test
        void should_return_false_when_addRespondent2_is_NO() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(NO);
            assertFalse(ClaimPredicate.submittedTwoRegisteredRespondentRepresentatives.test(caseData));
        }

        @Test
        void should_return_false_when_same_legal_representative() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
            assertFalse(ClaimPredicate.submittedTwoRegisteredRespondentRepresentatives.test(caseData));
        }

        @Test
        void should_return_false_when_respondent1_unrepresented() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            when(caseData.getRespondent1Represented()).thenReturn(NO);
            assertFalse(ClaimPredicate.submittedTwoRegisteredRespondentRepresentatives.test(caseData));
        }

        @Test
        void should_return_false_when_respondent2_unrepresented() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent2Represented()).thenReturn(NO);
            assertFalse(ClaimPredicate.submittedTwoRegisteredRespondentRepresentatives.test(caseData));
        }

        @Test
        void should_return_false_when_respondent1_org_not_registered() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent2Represented()).thenReturn(YES);
            assertFalse(ClaimPredicate.submittedTwoRegisteredRespondentRepresentatives.test(caseData));
        }

        @Test
        void should_return_false_when_respondent2_org_not_registered() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent2Represented()).thenReturn(YES);
            when(caseData.getRespondent1OrgRegistered()).thenReturn(YES);
            when(caseData.getRespondent2OrgRegistered()).thenReturn(NO);
            assertFalse(ClaimPredicate.submittedTwoRegisteredRespondentRepresentatives.test(caseData));
        }
    }

    @Nested
    class SubmittedTwoRespondentRepresentativesOneUnregisteredPredicate {

        @Test
        void should_return_true_when_r1_registered_and_r2_not_registered() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent2Represented()).thenReturn(YES);
            when(caseData.getRespondent1OrgRegistered()).thenReturn(YES);
            when(caseData.getRespondent2OrgRegistered()).thenReturn(NO);
            assertTrue(ClaimPredicate.submittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        }

        @Test
        void should_return_true_when_r2_registered_and_r1_not_registered() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent2Represented()).thenReturn(YES);
            when(caseData.getRespondent1OrgRegistered()).thenReturn(NO);
            when(caseData.getRespondent2OrgRegistered()).thenReturn(YES);
            assertTrue(ClaimPredicate.submittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        }

        @Test
        void should_return_false_when_no_submitted_date() {
            when(caseData.getSubmittedDate()).thenReturn(null);
            assertFalse(ClaimPredicate.submittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        }

        @Test
        void should_return_false_when_addRespondent2_is_NO() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(NO);
            assertFalse(ClaimPredicate.submittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        }

        @Test
        void should_return_false_when_same_legal_representative() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
            assertFalse(ClaimPredicate.submittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        }

        @Test
        void should_return_false_when_respondent1_unrepresented() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            when(caseData.getRespondent1Represented()).thenReturn(NO);
            assertFalse(ClaimPredicate.submittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        }

        @Test
        void should_return_false_when_respondent2_unrepresented() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent2Represented()).thenReturn(NO);
            assertFalse(ClaimPredicate.submittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        }

        @Test
        void should_return_false_when_both_organisations_registered() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent2Represented()).thenReturn(YES);
            when(caseData.getRespondent1OrgRegistered()).thenReturn(YES);
            when(caseData.getRespondent2OrgRegistered()).thenReturn(YES);
            assertFalse(ClaimPredicate.submittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        }

        @Test
        void should_return_false_when_both_organisations_unregistered() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            when(caseData.getRespondent1Represented()).thenReturn(YES);
            when(caseData.getRespondent2Represented()).thenReturn(YES);
            when(caseData.getRespondent1OrgRegistered()).thenReturn(NO);
            when(caseData.getRespondent2OrgRegistered()).thenReturn(NO);
            assertFalse(ClaimPredicate.submittedTwoRespondentRepresentativesOneUnregistered.test(caseData));
        }
    }

    @Nested
    class SubmittedBothUnregisteredSolicitorsPredicate {

        @Test
        void should_return_true_when_all_conditions_met_with_different_legal_reps() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1OrgRegistered()).thenReturn(NO);
            when(caseData.getRespondent2OrgRegistered()).thenReturn(NO);
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            assertTrue(ClaimPredicate.submittedBothUnregisteredSolicitors.test(caseData));
        }

        @Test
        void should_return_true_when_same_legal_rep_field_missing() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1OrgRegistered()).thenReturn(NO);
            when(caseData.getRespondent2OrgRegistered()).thenReturn(NO);
            when(caseData.getAddRespondent2()).thenReturn(YES);
            assertTrue(ClaimPredicate.submittedBothUnregisteredSolicitors.test(caseData));
        }

        @Test
        void should_return_false_when_no_submitted_date() {
            when(caseData.getSubmittedDate()).thenReturn(null);
            assertFalse(ClaimPredicate.submittedBothUnregisteredSolicitors.test(caseData));
        }

        @Test
        void should_return_false_when_respondent1_org_registered() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1OrgRegistered()).thenReturn(YES);
            assertFalse(ClaimPredicate.submittedBothUnregisteredSolicitors.test(caseData));
        }

        @Test
        void should_return_false_when_addRespondent2_is_NO() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1OrgRegistered()).thenReturn(NO);
            when(caseData.getAddRespondent2()).thenReturn(NO);
            assertFalse(ClaimPredicate.submittedBothUnregisteredSolicitors.test(caseData));
        }

        @Test
        void should_return_false_when_respondent2_org_registered() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1OrgRegistered()).thenReturn(NO);
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2OrgRegistered()).thenReturn(YES);
            assertFalse(ClaimPredicate.submittedBothUnregisteredSolicitors.test(caseData));
        }

        @Test
        void should_return_false_when_same_legal_representative_yes() {
            when(caseData.getSubmittedDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent1OrgRegistered()).thenReturn(NO);
            when(caseData.getAddRespondent2()).thenReturn(YES);
            when(caseData.getRespondent2OrgRegistered()).thenReturn(NO);
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
            assertFalse(ClaimPredicate.submittedBothUnregisteredSolicitors.test(caseData));
        }
    }
}
