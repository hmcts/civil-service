package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

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
    public void applicant_partAdmitClaimSettled() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .build();
        Assertions.assertTrue(caseData.isPartAdmitClaimSettled());
    }

    @Test
    public void applicant_partAdmitClaimNotSettled() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.NO)
            .build();
        Assertions.assertTrue(caseData.isPartAdmitClaimNotSettled());
    }

    @Test
    public void applicant_isClaimPartAdmitSpec() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .build();
        Assertions.assertTrue(caseData.isPartAdmitClaimSpec());
    }

    @Test
    public void applicant_isPartAdmitIntentionToSettleClaim() {
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .build();
        Assertions.assertTrue(caseData.isClaimantIntentionSettlePartAdmit());
    }

    @Test
    public void applicant_isPartAdmitIntentionNotToSettleClaim() {
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO)
            .build();
        Assertions.assertTrue(caseData.isClaimantIntentionNotSettlePartAdmit());
    }

    @Test
    public void applicant_isPartAdmitConfirmAmountPaid() {
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .build();
        Assertions.assertTrue(caseData.isClaimantConfirmAmountPaidPartAdmit());
    }

    @Test
    public void applicant_isPartAdmitConfirmAmountNotPaid() {
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.NO)
            .build();
        Assertions.assertTrue(caseData.isClaimantConfirmAmountNotPaidPartAdmit());
    }
}
