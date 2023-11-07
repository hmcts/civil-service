package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.RecurringExpenseLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RecurringIncomeLRspec;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.math.BigDecimal;
import java.util.Optional;

import java.util.List;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFENCE_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_ORDER;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class CaseDataTest {

    private static final BigDecimal CLAIM_FEE = new BigDecimal(2000);

    private static final String FILE_NAME_1 = "Some file 1";

    @Test
    public void applicant1Proceed_when1v1() {
        CaseData caseData = CaseData.builder()
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .build();
        assertEquals(YesOrNo.YES, caseData.getApplicant1ProceedsWithClaimSpec());
    }

    @Test
    public void applicant1Proceed_when2v1() {
        CaseData caseData = CaseData.builder()
            .applicant1ProceedWithClaimSpec2v1(YesOrNo.YES)
            .build();
        assertEquals(YesOrNo.YES, caseData.getApplicant1ProceedsWithClaimSpec());
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
        assertTrue(caseData.isPartAdmitClaimSettled());
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
        assertTrue(caseData.isPartAdmitClaimNotSettled());
    }

    @Test
    void applicant_isClaimPartAdmitSpec() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .build();
        //When
        //Then
        assertTrue(caseData.isPartAdmitClaimSpec());
    }

    @Test
    void applicant_isPartAdmitIntentionToSettleClaim() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .build();
        //When
        //Then
        assertTrue(caseData.isClaimantIntentionSettlePartAdmit());
    }

    @Test
    void applicant_isPartAdmitIntentionNotToSettleClaim() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO)
            .build();
        //When
        //Then
        assertTrue(caseData.isClaimantIntentionNotSettlePartAdmit());
    }

    @Test
    void applicant_isPartAdmitConfirmAmountPaid() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .build();
        //When
        //Then
        assertTrue(caseData.isClaimantConfirmAmountPaidPartAdmit());
    }

    @Test
    void applicant_isPartAdmitConfirmAmountNotPaid() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.NO)
            .build();
        assertTrue(caseData.isClaimantConfirmAmountNotPaidPartAdmit());
    }

    @Test
    public void givenRespondentUnrepresentedAndOnevOne_whenIsLRvLipOneVOne_thenTrue() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.YES)
            .respondent1(Party.builder().build())
            .applicant1(Party.builder().build())
            .build();
        //Then
        assertTrue(caseData.isLRvLipOneVOne());
    }

    @Test
    public void givenRespondentRepresentedAndOnevOne_whenIsLRvLipOneVOne_thenFalse() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.YES)
            .applicant1Represented(YesOrNo.YES)
            .respondent1(Party.builder().build())
            .applicant1(Party.builder().build())
            .build();
        //Then
        assertFalse(caseData.isLRvLipOneVOne());
    }

    @Test
    public void givenApplicantUnrepresentedAndOnevOne_whenIsLRvLipOneVOne_thenFalse() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().build())
            .applicant1(Party.builder().build())
            .build();
        //Then
        assertFalse(caseData.isLRvLipOneVOne());
    }

    @Test
    public void givenRespondentUnrepresentedAndApplicantUnrepresentedAndOnevOne_whenIsLipvLipOneVOne_thenTrue() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .respondent1(Party.builder().build())
            .applicant1(Party.builder().build())
            .build();
        //Then
        assertTrue(caseData.isLipvLipOneVOne());
    }

    @Test
    public void givenApplicantUnrepresented_whenIsApplicant1NotRepresented_thenTrue() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1Represented(YesOrNo.NO)
            .applicant1(Party.builder().build())
            .build();
        //Then
        assertTrue(caseData.isApplicant1NotRepresented());
    }

    @Test
    public void givenApplicantRepresented_whenIsApplicant1NotRepresented_thenFalse() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1Represented(YesOrNo.YES)
            .applicant1(Party.builder().build())
            .build();
        //Then
        assertFalse(caseData.isApplicant1NotRepresented());
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
        assertTrue(caseData.isClaimantNotSettlePartAdmitClaim());
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
        assertFalse(caseData.isClaimantNotSettlePartAdmitClaim());
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
        assertEquals(YesOrNo.YES, caseData.doesPartPaymentRejectedOrItsFullDefenceResponse());
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
        assertEquals(YesOrNo.YES, caseData.doesPartPaymentRejectedOrItsFullDefenceResponse());
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
        assertEquals(YesOrNo.NO, caseData.doesPartPaymentRejectedOrItsFullDefenceResponse());
    }

    @Test
    void hasDefendantNotAgreedToFreeMediation_Yes() {
        //Given
        CaseData caseData = CaseData.builder()
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .build();
        //When
        //Then
        assertFalse(caseData.hasDefendantNotAgreedToFreeMediation());
    }

    @Test
    void hasDefendantNotAgreedToFreeMediation_No() {
        //Given
        CaseData caseData = CaseData.builder()
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .build();
        //When
        //Then
        assertTrue(caseData.hasDefendantNotAgreedToFreeMediation());
    }

    @Test
    void isFastTrackClaim_thenTrue() {
        //Given
        CaseData caseData = CaseData.builder()
            .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
            .build();
        //When
        //Then
        assertTrue(caseData.isFastTrackClaim());
    }

    @Test
    void isFastTrackClaim_thenFalse() {
        //Given
        CaseData caseData = CaseData.builder()
            .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
            .build();
        //When
        //Then
        assertFalse(caseData.isFastTrackClaim());
    }

    @Test
    void isSmallClaim_thenTrue() {
        //Given
        CaseData caseData = CaseData.builder()
            .responseClaimTrack(AllocatedTrack.SMALL_CLAIM.name())
            .build();
        //When
        //Then
        assertTrue(caseData.isSmallClaim());
    }

    @Test
    void isSmallClaim_thenFalse() {
        //Given
        CaseData caseData = CaseData.builder()
            .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
            .build();
        //When
        //Then
        assertFalse(caseData.isSmallClaim());
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
        assertFalse(caseData.isRejectWithNoMediation());
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
        assertTrue(caseData.isRejectWithNoMediation());
    }

    @Test
    void shouldGetApplicantOrganisationId_whenOrganisationDetailsArePresent() {
        //Given
        String organisationId = "1245";
        CaseData caseData = CaseData.builder()
            .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                              .organisation(Organisation.builder()
                                                                .organisationID(organisationId)
                                                                .build())
                                              .build())
            .build();
        //When
        String result = caseData.getApplicantOrganisationId();
        //Then
        assertThat(result).isEqualTo(organisationId);
    }

    @Test
    void shouldReturnEmptyString_whenNoOrganisationDetailsArePresent() {
        //Given
        CaseData caseData = CaseData.builder().build();
        //When
        String result = caseData.getApplicantOrganisationId();
        //Then
        assertThat(result).isEmpty();
    }

    @Test
    void isTranslatedDocumentUploaded_thenFalse() {
        //Given
        CaseData caseData = CaseData.builder()
            .systemGeneratedCaseDocuments(null).build();
        //When
        //Then
        assertFalse(caseData.isTranslatedDocumentUploaded());
    }

    @Test
    void isTranslatedDocumentUploaded_thenTrue() {
        //Given
        CaseData caseData = CaseData.builder()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(DEFENCE_TRANSLATED_DOCUMENT).build())).build();
        //When
        //Then
        assertTrue(caseData.isTranslatedDocumentUploaded());
    }

    @Test
    void getSDOOrderDocument_WhenItPresent() {
        CaseData caseData = CaseData.builder()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SDO_ORDER).build())).build();
        //When
        Optional<Element<CaseDocument>> caseDocument = caseData.getSDODocument();
        //Then
        assertEquals(caseDocument.get().getValue().getDocumentType(), SDO_ORDER);
    }

    @Test
    void getSDOOrderDocument_WhenItsNull() {
        CaseData caseData = CaseData.builder()
            .systemGeneratedCaseDocuments(null).build();
        //When
        Optional<Element<CaseDocument>> caseDocument = caseData.getSDODocument();
        //Then
        assertTrue(caseDocument.isEmpty());
    }

    void isPartAdmitPayImmediatelyAccepted_thenTrue() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1AcceptAdmitAmountPaidSpec(YES)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .caseAccessCategory(SPEC_CLAIM)
            .build();
        //When
        //Then
        assertTrue(caseData.isPartAdmitPayImmediatelyAccepted());
    }

    @Test
    void isPartAdmitPayImmediatelyAccepted_thenFalse() {
        //Given
        CaseData caseData = CaseData.builder().build();
        //When
        //Then
        assertFalse(caseData.isPartAdmitPayImmediatelyAccepted());
    }

    @Test
    void shouldReturnTrueWhenResponseIsFullAdmit() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
            .build();
        assertTrue(caseData.isFullAdmitClaimSpec());
    }

    @Test
    void shouldReturnFalseWhenResponseIsNotFullAdmit() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .build();
        assertFalse(caseData.isFullAdmitClaimSpec());
    }

    @Test
    void shouldReturnRecurringIncomeForFullAdmitWhenTheyExist() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
            .respondent1DQ(Respondent1DQ.builder().respondent1DQRecurringIncomeFA(List.of(element(
                RecurringIncomeLRspec.builder().build()))).build())
            .build();
        //When
        List<Element<RecurringIncomeLRspec>> results = caseData.getRecurringIncomeForRespondent1();
        //Then
        assertThat(results).isNotNull();
    }

    @Test
    void shouldReturnRecurringIncomeForNonFullAdmitCaseWhenTheyExist() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .respondent1DQ(Respondent1DQ.builder().respondent1DQRecurringIncome(List.of(element(
                RecurringIncomeLRspec.builder().build()))).build())
            .build();
        //When
        List<Element<RecurringIncomeLRspec>> results = caseData.getRecurringIncomeForRespondent1();
        //Then
        assertThat(results).isNotNull();
    }

    @Test
    void shouldReturnNullForRecurringIncomeWhenRespondentDqIsNull() {
        //Given
        CaseData caseData = CaseData.builder().build();
        //When
        List<Element<RecurringIncomeLRspec>> results = caseData.getRecurringIncomeForRespondent1();
        //Then
        assertThat(results).isNull();
    }

    @Test
    void shouldReturnRecurringExpensesForFullAdmitWhenTheyExist() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
            .respondent1DQ(Respondent1DQ.builder().respondent1DQRecurringExpensesFA(List.of(element(
                RecurringExpenseLRspec.builder().build()))).build())
            .build();

        //When
        List<Element<RecurringExpenseLRspec>> results = caseData.getRecurringExpensesForRespondent1();
        //Then
        assertThat(results).isNotNull();
    }

    @Test
    void shouldReturnRecurringExpensesForNonFullAdmitWhenTheyExist() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
            .respondent1DQ(Respondent1DQ.builder().respondent1DQRecurringExpenses(List.of(element(
                RecurringExpenseLRspec.builder().build()))).build())
            .build();

        //When
        List<Element<RecurringExpenseLRspec>> results = caseData.getRecurringExpensesForRespondent1();
        //Then
        assertThat(results).isNotNull();
    }

    @Test
    void shouldReturnNullForRecurringExpensesWhenRespondent1DQIsNull() {
        //Given
        CaseData caseData = CaseData.builder().build();
        //When
        List<Element<RecurringExpenseLRspec>> results = caseData.getRecurringExpensesForRespondent1();
        //Then
        assertThat(results).isNull();
    }

    @Test
    void isApplicationDeadlineNotPassed_thenFalse() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().build();
        //When
        //Then
        assertFalse(caseData.getApplicant1ResponseDeadlinePassed());
    }

    @Test
    void isApplicationDeadlinePassed_thenTrue() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStatePastApplicantResponseDeadline().build();
        //When
        //Then
        assertTrue(caseData.getApplicant1ResponseDeadlinePassed());
    }

    @Test
    void shouldReturnEmptyArrayListOfManageDocumentsIfNull() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().build();
        //When
        //Then
        assertThat(caseData.getManageDocumentsList()).isNotNull();
        assertThat(caseData.getManageDocumentsList()).isEmpty();

    }

    @Test
    void shouldReturnClaimFeeInPence_whenClaimFeeExists() {
        //Given
        CaseData caseData = CaseData.builder()
            .claimFee(Fee.builder().calculatedAmountInPence(CLAIM_FEE).build())
            .build();
        //When
        BigDecimal fee = caseData.getCalculatedClaimFeeInPence();
        //Then
        assertThat(fee).isEqualTo(CLAIM_FEE);
    }

    @Test
    void shouldReturnClaimAmountBreakupDetails_whenExists() {
        //Given
        CaseData caseData = CaseData.builder()
            .claimAmountBreakup(List.of(ClaimAmountBreakup.builder()
                                            .id("1").value(ClaimAmountBreakupDetails.builder()
                                                               .claimAmount(new BigDecimal("122"))
                                                               .claimReason("Reason")
                                                               .build())
                                            .build()))
            .build();
        //When
        List<ClaimAmountBreakupDetails> result = caseData.getClaimAmountBreakupDetails();
        //Then
        assertThat(result).isNotEmpty();
    }

    @Test
    void shouldReturnZero_whenClaimFeeIsNull() {
        //Given
        CaseData caseData = CaseData.builder().build();
        //When
        BigDecimal fee = caseData.getCalculatedClaimFeeInPence();
        //Then
        assertThat(fee).isEqualTo(ZERO);
    }
}


