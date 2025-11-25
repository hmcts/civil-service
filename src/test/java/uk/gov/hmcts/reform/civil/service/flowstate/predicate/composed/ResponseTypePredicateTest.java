package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class ResponseTypePredicateTest {

    @Mock
    private CaseData caseData;

    @Test
    void should_return_true_for_is_when_one_v_one_and_respondent1_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        assertTrue(ResponseTypePredicate.is(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_is_when_one_v_one_and_respondent1_not_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        assertFalse(ResponseTypePredicate.is(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_is_when_one_v_one_and_no_response_date() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(null);
        assertFalse(ResponseTypePredicate.is(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_is_when_one_v_two_one_legal_rep_and_r1_full_defence_and_same_response() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondentResponseIsSame()).thenReturn(YesOrNo.YES);
        assertTrue(ResponseTypePredicate.is(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_is_when_one_v_two_one_legal_rep_and_r1_full_defence_and_r2_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondentResponseIsSame()).thenReturn(YesOrNo.NO);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        assertTrue(ResponseTypePredicate.is(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_is_when_one_v_two_one_legal_rep_and_r1_full_defence_and_r2_not_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondentResponseIsSame()).thenReturn(YesOrNo.NO);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        assertFalse(ResponseTypePredicate.is(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_is_when_one_v_two_two_legal_rep_and_both_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        assertTrue(ResponseTypePredicate.is(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_is_when_one_v_two_two_legal_rep_and_r1_not_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        assertFalse(ResponseTypePredicate.is(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_is_when_one_v_two_two_legal_rep_and_r2_not_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondent2ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        assertFalse(ResponseTypePredicate.is(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_is_when_two_v_one_and_r1_full_defence_and_r1_to_a2_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddApplicant2()).thenReturn(YES);
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondent1ClaimResponseTypeToApplicant2()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        assertTrue(ResponseTypePredicate.is(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_is_when_two_v_one_and_r1_not_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddApplicant2()).thenReturn(YES);
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.PART_ADMISSION);
        assertFalse(ResponseTypePredicate.is(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_is_when_two_v_one_and_r1_to_a2_not_full_defence() {
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddApplicant2()).thenReturn(YES);
        when(caseData.getRespondent1ClaimResponseType()).thenReturn(RespondentResponseType.FULL_DEFENCE);
        when(caseData.getRespondent1ClaimResponseTypeToApplicant2()).thenReturn(RespondentResponseType.PART_ADMISSION);
        assertFalse(ResponseTypePredicate.is(RespondentResponseType.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_isSpec_when_one_v_one_and_spec_claim_and_r1_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertTrue(ResponseTypePredicate.is(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_isSpec_when_one_v_one_and_spec_claim_and_r1_not_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        assertFalse(ResponseTypePredicate.is(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_isSpec_when_one_v_two_one_legal_rep_and_spec_claim_and_r1_full_defence_and_same_response() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getRespondentResponseIsSame()).thenReturn(YesOrNo.YES);
        assertTrue(ResponseTypePredicate.is(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_isSpec_when_one_v_two_one_legal_rep_and_spec_claim_and_r1_full_defence_and_r2_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getRespondentResponseIsSame()).thenReturn(YesOrNo.NO);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertTrue(ResponseTypePredicate.is(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_isSpec_when_one_v_two_one_legal_rep_and_spec_claim_and_r1_full_defence_and_r2_not_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent2SameLegalRepresentative()).thenReturn(YES);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getRespondentResponseIsSame()).thenReturn(YesOrNo.NO);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        assertFalse(ResponseTypePredicate.is(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_isSpec_when_one_v_two_two_legal_rep_and_spec_claim_and_both_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertTrue(ResponseTypePredicate.is(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_isSpec_when_one_v_two_two_legal_rep_and_spec_claim_and_r1_not_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        assertFalse(ResponseTypePredicate.is(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_isSpec_when_one_v_two_two_legal_rep_and_spec_claim_and_r2_not_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getRespondent2()).thenReturn(Party.builder().build());
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        assertFalse(ResponseTypePredicate.is(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_isSpec_when_two_v_one_and_defendant_single_response_and_r1_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddApplicant2()).thenReturn(YES);
        when(caseData.getDefendantSingleResponseToBothClaimants()).thenReturn(YesOrNo.YES);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertTrue(ResponseTypePredicate.is(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_isSpec_when_two_v_one_and_defendant_single_response_and_r1_not_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddApplicant2()).thenReturn(YES);
        when(caseData.getDefendantSingleResponseToBothClaimants()).thenReturn(YesOrNo.YES);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        assertFalse(ResponseTypePredicate.is(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_true_for_isSpec_when_two_v_one_and_not_defendant_single_response_and_both_claimants_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddApplicant2()).thenReturn(YES);
        when(caseData.getDefendantSingleResponseToBothClaimants()).thenReturn(YesOrNo.NO);
        when(caseData.getClaimant1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getClaimant2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertTrue(ResponseTypePredicate.is(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_isSpec_when_two_v_one_and_not_defendant_single_response_and_c1_not_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddApplicant2()).thenReturn(YES);
        when(caseData.getDefendantSingleResponseToBothClaimants()).thenReturn(YesOrNo.NO);
        when(caseData.getClaimant1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        assertFalse(ResponseTypePredicate.is(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }

    @Test
    void should_return_false_for_isSpec_when_two_v_one_and_not_defendant_single_response_and_c2_not_full_defence() {
        when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
        when(caseData.getRespondent1ResponseDate()).thenReturn(LocalDateTime.now());
        when(caseData.getAddApplicant2()).thenReturn(YES);
        when(caseData.getDefendantSingleResponseToBothClaimants()).thenReturn(YesOrNo.NO);
        when(caseData.getClaimant1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getClaimant2ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        assertFalse(ResponseTypePredicate.is(RespondentResponseTypeSpec.FULL_DEFENCE).test(caseData));
    }
}
