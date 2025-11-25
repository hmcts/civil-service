package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class ClaimantIntentionPredicateTest {

    @Mock
    private CaseData caseData;

    @Nested
    class FullDefenceProceed {

        @Test
        void should_return_true_for_fullDefenceProceed_when_1v1_spec_claim_and_applicant_will_proceed() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getApplicant1ProceedWithClaim()).thenReturn(YES);

            assertTrue(ClaimantIntentionPredicate.fullDefenceProceed.test(caseData));
        }

        @Test
        void should_return_false_for_fullDefenceProceed_when_1v1_spec_claim_and_applicant_will_not_proceed() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getApplicant1ProceedWithClaim()).thenReturn(NO);

            assertFalse(ClaimantIntentionPredicate.fullDefenceProceed.test(caseData));
        }

        @Test
        void should_return_true_for_fullDefenceProceed_when_2v1_spec_claim_and_both_applicants_will_proceed() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getApplicant1ProceedWithClaimSpec2v1()).thenReturn(YES);

            assertTrue(ClaimantIntentionPredicate.fullDefenceProceed.test(caseData));
        }

        @Test
        void should_return_false_for_fullDefenceProceed_when_2v1_spec_claim_and_one_applicant_will_not_proceed() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getApplicant1ProceedWithClaimSpec2v1()).thenReturn(NO);

            assertFalse(ClaimantIntentionPredicate.fullDefenceProceed.test(caseData));
        }

        @Test
        void should_return_true_for_fullDefenceProceed_when_1v1_unspec_claim_and_applicant_will_proceed() {
            when(caseData.getApplicant1ProceedWithClaim()).thenReturn(YES);

            assertTrue(ClaimantIntentionPredicate.fullDefenceProceed.test(caseData));
        }

        @ParameterizedTest
        @EnumSource(value = YesOrNo.class)
        void should_return_correctly_for_fullDefenceProceed_when_1v2_unspec_claim_and_applicant_will_proceed_against_one_respondent(
            YesOrNo proceed) {
            when(caseData.getRespondent2()).thenReturn(Party.builder().build());
            when(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()).thenReturn(proceed);

            if (proceed == YES) {
                assertTrue(ClaimantIntentionPredicate.fullDefenceProceed.test(caseData));
            } else {
                assertFalse(ClaimantIntentionPredicate.fullDefenceProceed.test(caseData));
            }
        }

        @Test
        void should_return_true_for_fullDefenceProceed_when_2v1_unspec_claim_and_applicant_one_will_proceed() {
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getApplicant1ProceedWithClaimMultiParty2v1()).thenReturn(YES);

            assertTrue(ClaimantIntentionPredicate.fullDefenceProceed.test(caseData));
        }

        @Test
        void should_return_true_for_fullDefenceProceed_when_2v1_unspec_claim_and_applicant_two_will_proceed() {
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getApplicant2ProceedWithClaimMultiParty2v1()).thenReturn(YES);

            assertTrue(ClaimantIntentionPredicate.fullDefenceProceed.test(caseData));
        }
    }

    @Nested
    class FullDefenceNotProceed {

        @Test
        void should_return_true_for_fullDefenceNotProceed_when_1v1_spec_claim_and_applicant_will_not_proceed() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getApplicant1ProceedWithClaim()).thenReturn(NO);

            assertTrue(ClaimantIntentionPredicate.fullDefenceNotProceed.test(caseData));
        }

        @Test
        void should_return_true_for_fullDefenceNotProceed_when_2v1_spec_claim_and_both_applicants_will_not_proceed() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getApplicant1ProceedWithClaimSpec2v1()).thenReturn(NO);

            assertTrue(ClaimantIntentionPredicate.fullDefenceNotProceed.test(caseData));
        }

        @Test
        void should_return_true_for_fullDefenceNotProceed_when_1v1_unspec_claim_and_applicant_will_not_proceed() {
            when(caseData.getApplicant1ProceedWithClaim()).thenReturn(NO);

            assertTrue(ClaimantIntentionPredicate.fullDefenceNotProceed.test(caseData));
        }

        @Test
        void should_return_true_for_fullDefenceNotProceed_when_1v2_unspec_claim_and_applicant_will_not_proceed_against_both_respondents() {
            when(caseData.getRespondent2()).thenReturn(Party.builder().build());
            when(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()).thenReturn(NO);
            when(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2()).thenReturn(NO);

            assertTrue(ClaimantIntentionPredicate.fullDefenceNotProceed.test(caseData));
        }

        @Test
        void should_return_true_for_fullDefenceNotProceed_when_2v1_unspec_claim_and_both_applicants_will_not_proceed() {
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getApplicant1ProceedWithClaimMultiParty2v1()).thenReturn(NO);
            when(caseData.getApplicant2ProceedWithClaimMultiParty2v1()).thenReturn(NO);

            assertTrue(ClaimantIntentionPredicate.fullDefenceNotProceed.test(caseData));
        }
    }
}
