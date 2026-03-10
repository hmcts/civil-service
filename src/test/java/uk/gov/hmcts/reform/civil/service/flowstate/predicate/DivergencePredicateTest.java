package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class DivergencePredicateTest {

    @Mock
    private CaseData caseData;

    @Nested
    class DivergentRespondWithDQAndGoOffline {

        @Test
        void should_return_true_for_divergentRespondWithDQAndGoOffline_when_1v2_one_rep_responses_differ_and_one_is_full_defence() {
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(FULL_DEFENCE);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(PART_ADMISSION);
            assertTrue(DivergencePredicate.divergentRespondWithDQAndGoOffline.test(caseData));
        }

        @Test
        void should_return_true_for_divergentRespondWithDQAndGoOffline_when_1v2_one_rep_responses_differ_and_two_is_full_defence() {
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(PART_ADMISSION);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(FULL_DEFENCE);
            assertTrue(DivergencePredicate.divergentRespondWithDQAndGoOffline.test(caseData));
        }

        @Test
        void should_return_false_for_divergentRespondWithDQAndGoOffline_when_1v2_one_rep_responses_are_same() {
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(FULL_DEFENCE);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(FULL_DEFENCE);
            assertFalse(DivergencePredicate.divergentRespondWithDQAndGoOffline.test(caseData));
        }

        @Test
        void should_return_true_for_divergentRespondWithDQAndGoOffline_when_1v2_two_reps_responses_differ_and_one_full_defence_is_second() {
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(FULL_DEFENCE);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(PART_ADMISSION);
            when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent2ResponseDate()).thenReturn(LocalDateTime.now().minusDays(1));
            assertTrue(DivergencePredicate.divergentRespondWithDQAndGoOffline.test(caseData));
        }

        @Test
        void should_return_true_for_divergentRespondWithDQAndGoOffline_when_1v2_two_reps_responses_differ_and_two_full_defence_is_second() {
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(PART_ADMISSION);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(FULL_DEFENCE);
            when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent2ResponseDate()).thenReturn(LocalDateTime.now());
            assertTrue(DivergencePredicate.divergentRespondWithDQAndGoOffline.test(caseData));
        }

        @Test
        void should_return_false_for_divergentRespondWithDQAndGoOffline_when_1v2_two_reps_responses_differ_and_full_defence_is_first_but_responded_first() {
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(FULL_DEFENCE);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(PART_ADMISSION);
            when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent2ResponseDate()).thenReturn(LocalDateTime.now());
            assertFalse(DivergencePredicate.divergentRespondWithDQAndGoOffline.test(caseData));
        }

        @Test
        void should_return_false_for_divergentRespondWithDQAndGoOffline_when_1v2_two_reps_responses_are_same() {
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(FULL_DEFENCE);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(FULL_DEFENCE);
            assertFalse(DivergencePredicate.divergentRespondWithDQAndGoOffline.test(caseData));
        }

        @Test
        void should_return_true_for_divergentRespondWithDQAndGoOffline_when_2v1_one_response_is_full_defence() {
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(FULL_DEFENCE);
            when(caseData.getRespondent1ClaimResponseTypeToApplicant2()).thenReturn(PART_ADMISSION);
            assertTrue(DivergencePredicate.divergentRespondWithDQAndGoOffline.test(caseData));
        }

        @Test
        void should_return_true_for_divergentRespondWithDQAndGoOffline_when_2v1_applicant2_response_is_full_defence() {
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(PART_ADMISSION);
            when(caseData.getRespondent1ClaimResponseTypeToApplicant2()).thenReturn(FULL_DEFENCE);
            assertTrue(DivergencePredicate.divergentRespondWithDQAndGoOffline.test(caseData));
        }

        @Test
        void should_return_false_for_divergentRespondWithDQAndGoOffline_when_2v1_one_response_is_part_admission() {
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(PART_ADMISSION);
            when(caseData.getRespondent1ClaimResponseTypeToApplicant2()).thenReturn(PART_ADMISSION);
            assertFalse(DivergencePredicate.divergentRespondWithDQAndGoOffline.test(caseData));
        }

        @Test
        void should_return_false_for_divergentRespondWithDQAndGoOffline_when_2v1_responses_differ_and_no_full_defence() {
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(PART_ADMISSION);
            when(caseData.getRespondent1ClaimResponseTypeToApplicant2()).thenReturn(COUNTER_CLAIM);
            assertFalse(DivergencePredicate.divergentRespondWithDQAndGoOffline.test(caseData));
        }

        @Test
        void should_return_false_for_divergentRespondWithDQAndGoOffline_when_2v1_one_response_are_same() {
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(FULL_DEFENCE);
            when(caseData.getRespondent1ClaimResponseTypeToApplicant2()).thenReturn(FULL_DEFENCE);
            assertFalse(DivergencePredicate.divergentRespondWithDQAndGoOffline.test(caseData));
        }

        @Test
        void should_return_true_for_divergentRespondWithDQAndGoOffline_when_1v2_two_reps_responses_differ_and_full_defence_is_first() {
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(FULL_DEFENCE);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(PART_ADMISSION);
            when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent2ResponseDate()).thenReturn(LocalDateTime.now().minusDays(1));
            assertTrue(DivergencePredicate.divergentRespondWithDQAndGoOffline.test(caseData));
        }

        @Test
        void should_return_false_for_divergentRespondWithDQAndGoOffline_when_1v1() {
            assertFalse(DivergencePredicate.divergentRespondWithDQAndGoOffline.test(caseData));
        }
    }

    @Nested
    class DivergentRespondGoOffline {

        @Test
        void should_return_true_for_divergentRespondGoOffline_when_1v2_two_reps_responses_differ_and_no_full_defence() {
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(PART_ADMISSION);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(COUNTER_CLAIM);
            assertTrue(DivergencePredicate.divergentRespondGoOffline.test(caseData));
        }

        @Test
        void should_return_true_for_divergentRespondGoOffline_when_1v2_one_rep_responses_differ_and_no_full_defence() {
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(PART_ADMISSION);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(COUNTER_CLAIM);
            assertTrue(DivergencePredicate.divergentRespondGoOffline.test(caseData));
        }

        @Test
        void should_return_false_for_divergentRespondGoOffline_when_1v2_one_rep_responses_same_and_no_full_defence() {
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(PART_ADMISSION);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(PART_ADMISSION);
            assertFalse(DivergencePredicate.divergentRespondGoOffline.test(caseData));
        }

        @Test
        void should_return_false_for_divergentRespondGoOffline_when_1v2_one_rep_responses_differ_and_one_is_full_defence() {
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(FULL_DEFENCE);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(COUNTER_CLAIM);
            assertFalse(DivergencePredicate.divergentRespondGoOffline.test(caseData));
        }

        @Test
        void should_return_false_for_divergentRespondGoOffline_when_1v2_one_rep_responses_differ_and_two_is_full_defence() {
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(PART_ADMISSION);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(FULL_DEFENCE);
            assertFalse(DivergencePredicate.divergentRespondGoOffline.test(caseData));
        }

        @Test
        void should_return_true_for_divergentRespondGoOffline_when_2v1_no_full_defence() {
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(PART_ADMISSION);
            when(caseData.getRespondent1ClaimResponseTypeToApplicant2()).thenReturn(COUNTER_CLAIM);
            assertTrue(DivergencePredicate.divergentRespondGoOffline.test(caseData));
        }

        @Test
        void should_return_false_for_divergentRespondGoOffline_when_2v1_one_response_is_full_defence() {
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(FULL_DEFENCE);

            assertFalse(DivergencePredicate.divergentRespondGoOffline.test(caseData));
        }

        @Test
        void should_return_false_for_divergentRespondGoOffline_when_2v1_applicant2_response_is_full_defence() {
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(PART_ADMISSION);
            when(caseData.getRespondent1ClaimResponseTypeToApplicant2()).thenReturn(FULL_DEFENCE);
            assertFalse(DivergencePredicate.divergentRespondGoOffline.test(caseData));
        }

        @Test
        void should_return_true_for_divergentRespondGoOffline_when_1v2_two_reps_responses_differ_and_respondent2_responded_first() {
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(PART_ADMISSION);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(COUNTER_CLAIM);
            when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
            when(caseData.getRespondent2ResponseDate()).thenReturn(LocalDateTime.now().minusDays(1));
            assertTrue(DivergencePredicate.divergentRespondGoOffline.test(caseData));
        }

        @Test
        void should_return_true_for_divergentRespondGoOffline_when_1v2_two_reps_responses_differ_and_respondent1_responded_first() {
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            when(caseData.getRespondent1ClaimResponseType()).thenReturn(PART_ADMISSION);
            when(caseData.getRespondent2ClaimResponseType()).thenReturn(COUNTER_CLAIM);
            when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent2ResponseDate()).thenReturn(LocalDateTime.now());
            assertTrue(DivergencePredicate.divergentRespondGoOffline.test(caseData));
        }

        @Test
        void should_return_false_for_divergentRespondGoOffline_when_1v1() {
            when(caseData.getRespondent2()).thenReturn(null);
            when(caseData.getAddApplicant2()).thenReturn(NO);
            assertFalse(DivergencePredicate.divergentRespondGoOffline.test(caseData));
        }
    }

    @Nested
    class DivergentRespondWithDQAndGoOfflineSpec {

        @Test
        void should_return_true_for_divergentRespondWithDQAndGoOfflineSpec_when_spec_1v2_one_rep_responses_differ_and_one_is_full_defence() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
            when(caseData.getRespondentResponseIsSame()).thenReturn(NO);
            when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
            when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
            assertTrue(DivergencePredicate.divergentRespondWithDQAndGoOfflineSpec.test(caseData));
        }

        @Test
        void should_return_true_for_divergentRespondWithDQAndGoOfflineSpec_when_spec_1v2_two_reps_responses_differ() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(NO);
            when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
            when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
            when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now().minusDays(1));
            when(caseData.getRespondent2ResponseDate()).thenReturn(LocalDateTime.now());
            assertTrue(DivergencePredicate.divergentRespondWithDQAndGoOfflineSpec.test(caseData));
        }

        @Test
        void should_return_true_for_divergentRespondWithDQAndGoOfflineSpec_when_spec_2v1_one_response_is_full_defence() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getClaimant1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
            when(caseData.getClaimant2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
            assertTrue(DivergencePredicate.divergentRespondWithDQAndGoOfflineSpec.test(caseData));
        }

        @Test
        void should_return_false_for_divergentRespondWithDQAndGoOfflineSpec_when_1v1() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            assertFalse(DivergencePredicate.divergentRespondWithDQAndGoOfflineSpec.test(caseData));
        }
    }

    @Nested
    class DivergentRespondGoOfflineSpec {

        @Test
        void should_return_true_for_divergentRespondGoOfflineSpec_when_spec_1v2_one_rep_responses_differ_and_no_full_defence() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getRespondent2()).thenReturn(new Party());
            when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
            when(caseData.getRespondentResponseIsSame()).thenReturn(NO);
            when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
            when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.COUNTER_CLAIM);
            assertTrue(DivergencePredicate.divergentRespondGoOfflineSpec.test(caseData));
        }

        @Test
        void should_return_true_for_divergentRespondGoOfflineSpec_when_spec_2v1_no_full_defence_and_responses_differ() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            when(caseData.getAddApplicant2()).thenReturn(YES);
            when(caseData.getClaimant1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
            when(caseData.getClaimant2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.COUNTER_CLAIM);
            assertTrue(DivergencePredicate.divergentRespondGoOfflineSpec.test(caseData));
        }

        @Test
        void should_return_false_for_divergentRespondGoOfflineSpec_when_1v1() {
            when(caseData.getCaseAccessCategory()).thenReturn(SPEC_CLAIM);
            assertFalse(DivergencePredicate.divergentRespondGoOfflineSpec.test(caseData));
        }
    }
}
