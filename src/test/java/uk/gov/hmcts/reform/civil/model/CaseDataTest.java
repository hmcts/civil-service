package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;

public class CaseDataTest {

    @Test
    public void applicant1Proceed_when1v1() {
        CaseData caseData = CaseData.builder()
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .build();
        Assertions.assertEquals(YesOrNo.YES, caseData.getApplicant1ProceedsWithClaimSpec());
    }

    @Test
    public void applicant1Proceed_when2v1() {
        CaseData caseData = CaseData.builder()
            .applicant1ProceedWithClaimSpec2v1(YesOrNo.YES)
            .build();
        Assertions.assertEquals(YesOrNo.YES, caseData.getApplicant1ProceedsWithClaimSpec());
    }

    @Test
    void givenApplicantAgreedToMediation_whenHasClaimantAgreedToFreeMediation_thenTrue() {
        //Given
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                                                                          .hasAgreedFreeMediation(MediationDecision.Yes)
                                                                          .build())
                             .build())
            .build();
        //When
        boolean result = caseData.hasClaimantAgreedToFreeMediation();
        //Then
        assertThat(result).isTrue();
    }

    @Test
    void givenNoDataForAgreedToMediation_whenHasClaimantAgreedToFeeMediation_thenFalse() {
        //Given
        CaseData caseData = CaseData.builder().build();
        //When
        boolean result = caseData.hasClaimantAgreedToFreeMediation();
        //Then
        assertThat(result).isFalse();
    }

    @Test
    void givenApplicantDidNotAgreeToFreeMediation_whenHasClaimantAgreedToFeeMediation_thenFalse() {
        //Given
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                                                                          .hasAgreedFreeMediation(MediationDecision.No)
                                                                          .build())
                             .build())
            .build();
        //When
        boolean result = caseData.hasClaimantAgreedToFreeMediation();
        //Then
        assertThat(result).isFalse();
    }

    @Test
    void givenNotOneVTwoTwoLegalRepCaseResponseFullDefence_whenIsRespondentResponseFullDefence_thenTrue() {
        //Given
        CaseData caseData = CaseData.builder().respondent1ClaimResponseTypeForSpec(FULL_DEFENCE).build();
        //When
        boolean result = caseData.isRespondentResponseFullDefence();
        //Then
        assertThat(result).isTrue();
    }

    @Test
    void givenNotOneVTwoTwoLegalRepCaseResponsePartAdmit_whenIsRespondentResponseFullDefence_thenFalse() {
        //Given
        CaseData caseData = CaseData.builder().respondent1ClaimResponseTypeForSpec(PART_ADMISSION).build();
        //When
        boolean result = caseData.isRespondentResponseFullDefence();
        //Then
        assertThat(result).isFalse();
    }

    @Test
    void givenOneVTwoTwoLegalRepCaseRespondent1FullDefence_whenIsRespondentResponseFullDefence_thenFalse() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().build())
            .respondent2(PartyBuilder.builder().build())
            .applicant1(PartyBuilder.builder().build())
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .build();
        //When
        boolean result = caseData.isRespondentResponseFullDefence();
        //Then
        assertThat(result).isFalse();
    }

    @Test
    void givenOneVTwoTwoLegalRepCaseRespondent1And2FullDefence_whenIsRespondentResponseFullDefence_thenTrue() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1(PartyBuilder.builder().build())
            .respondent2(PartyBuilder.builder().build())
            .applicant1(PartyBuilder.builder().build())
            .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
            .respondent2ClaimResponseTypeForSpec(FULL_DEFENCE)
            .build();
        //When
        boolean result = caseData.isRespondentResponseFullDefence();
        //Then
        assertThat(result).isTrue();
    }

    @Test
    void applicant_partAdmitClaimSettled() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .build();
        //When
        //Then
        Assertions.assertTrue(caseData.isPartAdmitClaimSettled());
    }

    @Test
    void applicant_partAdmitClaimNotSettled() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.NO)
            .build();
        //When
        //Then
        Assertions.assertTrue(caseData.isPartAdmitClaimNotSettled());
    }

    @Test
    void applicant_isClaimPartAdmitSpec() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .build();
        //When
        //Then
        Assertions.assertTrue(caseData.isPartAdmitClaimSpec());
    }

    @Test
    void applicant_isPartAdmitIntentionToSettleClaim() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .build();
        //When
        //Then
        Assertions.assertTrue(caseData.isClaimantIntentionSettlePartAdmit());
    }

    @Test
    void applicant_isPartAdmitIntentionNotToSettleClaim() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO)
            .build();
        //When
        //Then
        Assertions.assertTrue(caseData.isClaimantIntentionNotSettlePartAdmit());
    }

    @Test
    void applicant_isPartAdmitConfirmAmountPaid() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .build();
        //When
        //Then
        Assertions.assertTrue(caseData.isClaimantConfirmAmountPaidPartAdmit());
    }

    @Test
    void applicant_isPartAdmitConfirmAmountNotPaid() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.NO)
            .build();
        //When
        //Then
        Assertions.assertTrue(caseData.isClaimantConfirmAmountNotPaidPartAdmit());
    }

    @Test
    void isClaimantNotSettlePartAdmitClaim_thenTrue() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.NO)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
            .build();
        //When
        //Then
        Assertions.assertTrue(caseData.isClaimantNotSettlePartAdmitClaim());
    }

    @Test
    void isClaimantNotSettlePartAdmitClaim_thenFalse() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .build();
        //When
        //Then
        Assertions.assertFalse(caseData.isClaimantNotSettlePartAdmitClaim());
    }

    @Test
    void doesPartPaymentRejectedOrItsFullDefenceResponse_fullDefence() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .applicant1ProceedWithClaimSpec2v1(YesOrNo.YES)
            .build();
        //When
        //Then
        Assertions.assertEquals(YesOrNo.YES, caseData.doesPartPaymentRejectedOrItsFullDefenceResponse());
    }

    @Test
    void doesPartPaymentRejectedOrItsFullDefenceResponse_partAdmitRejectYes() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.NO)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
            .build();
        //When
        //Then
        Assertions.assertEquals(YesOrNo.YES, caseData.doesPartPaymentRejectedOrItsFullDefenceResponse());
    }

    @Test
    void doesPartPaymentRejectedOrItsFullDefenceResponse_partAdmitRejectNo() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .build();
        //When
        //Then
        Assertions.assertEquals(YesOrNo.NO, caseData.doesPartPaymentRejectedOrItsFullDefenceResponse());
    }

    @Test
    void hasDefendantNotAgreedToFreeMediation_Yes() {
        //Given
        CaseData caseData = CaseData.builder()
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .build();
        //When
        //Then
        Assertions.assertFalse(caseData.hasDefendantNotAgreedToFreeMediation());
    }

    @Test
    void hasDefendantNotAgreedToFreeMediation_No() {
        //Given
        CaseData caseData = CaseData.builder()
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .build();
        //When
        //Then
        Assertions.assertTrue(caseData.hasDefendantNotAgreedToFreeMediation());
    }

    @Test
    void isFastTrackClaim_thenTrue() {
        //Given
        CaseData caseData = CaseData.builder()
            .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
            .build();
        //When
        //Then
        Assertions.assertTrue(caseData.isFastTrackClaim());
    }

    @Test
    void isFastTrackClaim_thenFalse() {
        //Given
        CaseData caseData = CaseData.builder()
            .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
            .build();
        //When
        //Then
        Assertions.assertFalse(caseData.isFastTrackClaim());
    }

    @Test
    void isSmallClaim_thenTrue() {
        //Given
        CaseData caseData = CaseData.builder()
            .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
            .build();
        //When
        //Then
        Assertions.assertTrue(caseData.isSmallClaim());
    }

    @Test
    void isSmallClaim_thenFalse() {
        //Given
        CaseData caseData = CaseData.builder()
            .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
            .build();
        //When
        //Then
        Assertions.assertFalse(caseData.isSmallClaim());
    }

    @Test
    void isRejectWithMediation_thenFalse() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .build();
        //When
        //Then
        Assertions.assertFalse(caseData.isRejectWithNoMediation());
    }

    @Test
    void isRejectWithMediation_thenTrue() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.NO)
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .build();
        //When
        //Then
        Assertions.assertTrue(caseData.isRejectWithNoMediation());
    }
}
