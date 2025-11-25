package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LipPredicateTest {

    @Mock
    private CaseData caseData;

    @Test
    void should_return_true_for_isLipCase_when_case_data_is_lip_v_lip_one_v_one() {
        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        assertTrue(LipPredicate.isLipCase.test(caseData));
    }

    @Test
    void should_return_false_for_isLipCase_when_case_data_is_not_lip_v_lip_one_v_one() {
        when(caseData.isLipvLipOneVOne()).thenReturn(false);
        assertFalse(LipPredicate.isLipCase.test(caseData));
    }

    @Test
    void should_return_true_for_agreedToMediation_when_claimant_has_agreed_to_free_mediation() {
        when(caseData.hasClaimantAgreedToFreeMediation()).thenReturn(true);
        assertTrue(LipPredicate.agreedToMediation.test(caseData));
    }

    @Test
    void should_return_false_for_agreedToMediation_when_claimant_has_not_agreed_to_free_mediation() {
        when(caseData.hasClaimantAgreedToFreeMediation()).thenReturn(false);
        assertFalse(LipPredicate.agreedToMediation.test(caseData));
    }

    @Test
    void should_return_true_for_isTranslatedDocumentUploaded_when_document_is_uploaded() {
        when(caseData.isTranslatedDocumentUploaded()).thenReturn(true);
        assertTrue(LipPredicate.isTranslatedDocumentUploaded.test(caseData));
    }

    @Test
    void should_return_false_for_isTranslatedDocumentUploaded_when_document_is_not_uploaded() {
        when(caseData.isTranslatedDocumentUploaded()).thenReturn(false);
        assertFalse(LipPredicate.isTranslatedDocumentUploaded.test(caseData));
    }

    @Test
    void should_return_true_for_ccjRequestJudgmentByAdmission_when_ccj_is_requested() {
        when(caseData.isCcjRequestJudgmentByAdmission()).thenReturn(true);
        assertTrue(LipPredicate.ccjRequestJudgmentByAdmission.test(caseData));
    }

    @Test
    void should_return_false_for_ccjRequestJudgmentByAdmission_when_ccj_is_not_requested() {
        when(caseData.isCcjRequestJudgmentByAdmission()).thenReturn(false);
        assertFalse(LipPredicate.ccjRequestJudgmentByAdmission.test(caseData));
    }

    @Test
    void should_return_true_for_isRespondentSignSettlementAgreement_when_respondent_has_responded() {
        when(caseData.isRespondentRespondedToSettlementAgreement()).thenReturn(true);
        assertTrue(LipPredicate.isRespondentSignSettlementAgreement.test(caseData));
    }

    @Test
    void should_return_false_for_isRespondentSignSettlementAgreement_when_respondent_has_not_responded() {
        when(caseData.isRespondentRespondedToSettlementAgreement()).thenReturn(false);
        assertFalse(LipPredicate.isRespondentSignSettlementAgreement.test(caseData));
    }

    @Test
    void should_return_true_for_nocSubmittedForLiPDefendantBeforeOffline_when_noc_is_submitted() {
        when(caseData.nocApplyForLiPDefendantBeforeOffline()).thenReturn(true);
        assertTrue(LipPredicate.nocSubmittedForLiPDefendantBeforeOffline.test(caseData));
    }

    @Test
    void should_return_false_for_nocSubmittedForLiPDefendantBeforeOffline_when_noc_is_not_submitted() {
        when(caseData.nocApplyForLiPDefendantBeforeOffline()).thenReturn(false);
        assertFalse(LipPredicate.nocSubmittedForLiPDefendantBeforeOffline.test(caseData));
    }

    @Test
    void should_return_true_for_nocSubmittedForLiPDefendant_when_noc_is_submitted() {
        when(caseData.nocApplyForLiPDefendant()).thenReturn(true);
        assertTrue(LipPredicate.nocSubmittedForLiPDefendant.test(caseData));
    }

    @Test
    void should_return_false_for_nocSubmittedForLiPDefendant_when_noc_is_not_submitted() {
        when(caseData.nocApplyForLiPDefendant()).thenReturn(false);
        assertFalse(LipPredicate.nocSubmittedForLiPDefendant.test(caseData));
    }

    @Test
    void should_return_true_for_caseContainsLiP_when_respondent1_is_lip() {
        when(caseData.isRespondent1LiP()).thenReturn(true);
        assertTrue(LipPredicate.caseContainsLiP.test(caseData));
    }

    @Test
    void should_return_true_for_caseContainsLiP_when_respondent2_is_lip() {
        when(caseData.isRespondent2LiP()).thenReturn(true);
        assertTrue(LipPredicate.caseContainsLiP.test(caseData));
    }

    @Test
    void should_return_true_for_caseContainsLiP_when_applicant_is_not_represented() {
        when(caseData.isApplicantNotRepresented()).thenReturn(true);
        assertTrue(LipPredicate.caseContainsLiP.test(caseData));
    }

    @Test
    void should_return_false_for_caseContainsLiP_when_no_party_is_lip() {
        when(caseData.isRespondent1LiP()).thenReturn(false);
        when(caseData.isRespondent2LiP()).thenReturn(false);
        when(caseData.isApplicantNotRepresented()).thenReturn(false);
        assertFalse(LipPredicate.caseContainsLiP.test(caseData));
    }
}
