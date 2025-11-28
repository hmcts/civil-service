package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

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
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class ClaimantPredicateTest {

    @Mock
    private CaseData caseData;

    @Nested
    class BeforeResponse {

        @Test
        void should_return_true_when_unspec_2v1_and_no_response_dates_for_both_applicants() {
            when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getApplicant1ResponseDate()).thenReturn(null);
            when(caseData.getApplicant2ResponseDate()).thenReturn(null);

            assertTrue(ClaimantPredicate.beforeResponse.test(caseData));
        }

        @Test
        void should_return_false_when_unspec_2v1_and_applicant1_has_response_date() {
            when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getApplicant1ResponseDate()).thenReturn(java.time.LocalDateTime.now());

            assertFalse(ClaimantPredicate.beforeResponse.test(caseData));
        }

        @Test
        void should_return_false_when_unspec_2v1_and_applicant2_has_response_date() {
            when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getApplicant1ResponseDate()).thenReturn(null);
            when(caseData.getApplicant2ResponseDate()).thenReturn(java.time.LocalDateTime.now());

            assertFalse(ClaimantPredicate.beforeResponse.test(caseData));
        }

        @Test
        void should_return_true_when_not_unspec_2v1_and_applicant1_has_no_response_date() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getApplicant1ResponseDate()).thenReturn(null);

            assertTrue(ClaimantPredicate.beforeResponse.test(caseData));
        }

        @Test
        void should_return_false_when_not_unspec_2v1_and_applicant1_has_response_date() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getApplicant1ResponseDate()).thenReturn(java.time.LocalDateTime.now());

            assertFalse(ClaimantPredicate.beforeResponse.test(caseData));
        }

        @Test
        void should_return_false_when_unspec_but_no_applicant2_added_and_applicant1_has_response_date() {
            when(caseData.getCaseAccessCategory()).thenReturn(UNSPEC_CLAIM);
            when(caseData.getAddApplicant2()).thenReturn(NO);
            when(caseData.getApplicant1ResponseDate()).thenReturn(java.time.LocalDateTime.now());

            assertFalse(ClaimantPredicate.beforeResponse.test(caseData));
        }
    }

    @Nested
    class FullDefenceProceed {

        @Test
        void should_return_true_for_fullDefenceProceed_when_1v1_spec_claim_and_applicant_will_proceed() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getApplicant1ProceedWithClaim()).thenReturn(YES);

            assertTrue(ClaimantPredicate.fullDefenceProceed.test(caseData));
        }

        @Test
        void should_return_false_for_fullDefenceProceed_when_1v1_spec_claim_and_applicant_will_not_proceed() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getApplicant1ProceedWithClaim()).thenReturn(NO);

            assertFalse(ClaimantPredicate.fullDefenceProceed.test(caseData));
        }

        @Test
        void should_return_true_for_fullDefenceProceed_when_2v1_spec_claim_and_both_applicants_will_proceed() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getApplicant1ProceedWithClaimSpec2v1()).thenReturn(YES);

            assertTrue(ClaimantPredicate.fullDefenceProceed.test(caseData));
        }

        @Test
        void should_return_false_for_fullDefenceProceed_when_2v1_spec_claim_and_one_applicant_will_not_proceed() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getApplicant1ProceedWithClaimSpec2v1()).thenReturn(NO);

            assertFalse(ClaimantPredicate.fullDefenceProceed.test(caseData));
        }

        @Test
        void should_return_true_for_fullDefenceProceed_when_1v1_unspec_claim_and_applicant_will_proceed() {
            when(caseData.getApplicant1ProceedWithClaim()).thenReturn(YES);

            assertTrue(ClaimantPredicate.fullDefenceProceed.test(caseData));
        }

        @ParameterizedTest
        @EnumSource(value = YesOrNo.class)
        void should_return_correctly_for_fullDefenceProceed_when_1v2_unspec_claim_and_applicant_will_proceed_against_one_respondent(
            YesOrNo proceed) {
            when(caseData.getRespondent2()).thenReturn(Party.builder().build());
            when(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()).thenReturn(proceed);

            if (proceed == YES) {
                assertTrue(ClaimantPredicate.fullDefenceProceed.test(caseData));
            } else {
                assertFalse(ClaimantPredicate.fullDefenceProceed.test(caseData));
            }
        }

        @Test
        void should_return_true_for_fullDefenceProceed_when_2v1_unspec_claim_and_applicant_one_will_proceed() {
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getApplicant1ProceedWithClaimMultiParty2v1()).thenReturn(YES);

            assertTrue(ClaimantPredicate.fullDefenceProceed.test(caseData));
        }

        @Test
        void should_return_true_for_fullDefenceProceed_when_2v1_unspec_claim_and_applicant_two_will_proceed() {
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getApplicant2ProceedWithClaimMultiParty2v1()).thenReturn(YES);

            assertTrue(ClaimantPredicate.fullDefenceProceed.test(caseData));
        }
    }

    @Nested
    class FullDefenceNotProceed {

        @Test
        void should_return_true_for_fullDefenceNotProceed_when_1v1_spec_claim_and_applicant_will_not_proceed() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getApplicant1ProceedWithClaim()).thenReturn(NO);

            assertTrue(ClaimantPredicate.fullDefenceNotProceed.test(caseData));
        }

        @Test
        void should_return_true_for_fullDefenceNotProceed_when_2v1_spec_claim_and_both_applicants_will_not_proceed() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getApplicant1ProceedWithClaimSpec2v1()).thenReturn(NO);

            assertTrue(ClaimantPredicate.fullDefenceNotProceed.test(caseData));
        }

        @Test
        void should_return_true_for_fullDefenceNotProceed_when_1v1_unspec_claim_and_applicant_will_not_proceed() {
            when(caseData.getApplicant1ProceedWithClaim()).thenReturn(NO);

            assertTrue(ClaimantPredicate.fullDefenceNotProceed.test(caseData));
        }

        @Test
        void should_return_true_for_fullDefenceNotProceed_when_1v2_unspec_claim_and_applicant_will_not_proceed_against_both_respondents() {
            when(caseData.getRespondent2()).thenReturn(Party.builder().build());
            when(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2()).thenReturn(NO);
            when(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2()).thenReturn(NO);

            assertTrue(ClaimantPredicate.fullDefenceNotProceed.test(caseData));
        }

        @Test
        void should_return_true_for_fullDefenceNotProceed_when_2v1_unspec_claim_and_both_applicants_will_not_proceed() {
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getApplicant1ProceedWithClaimMultiParty2v1()).thenReturn(NO);
            when(caseData.getApplicant2ProceedWithClaimMultiParty2v1()).thenReturn(NO);

            assertTrue(ClaimantPredicate.fullDefenceNotProceed.test(caseData));
        }
    }

}
