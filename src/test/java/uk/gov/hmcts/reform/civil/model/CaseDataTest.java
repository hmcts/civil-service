package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.RecurringExpenseLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RecurringIncomeLRspec;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFENCE_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_ORDER;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

class CaseDataTest {

    private static final BigDecimal CLAIM_FEE = new BigDecimal(2000);

    @Test
    void applicant1Proceed_when1v1() {
        CaseData caseData = CaseData.builder()
            .applicant1ProceedWithClaim(YesOrNo.YES)
            .build();
        assertEquals(YesOrNo.YES, caseData.getApplicant1ProceedsWithClaimSpec());
    }

    @Test
    void applicant1Proceed_when2v1() {
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
    void givenRespondentUnrepresentedAndOnevOne_whenIsLRvLipOneVOne_thenTrue() {
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
    void givenRespondentRepresentedAndOnevOne_whenIsLRvLipOneVOne_thenFalse() {
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
    void givenApplicantUnrepresentedAndOnevOne_whenIsLRvLipOneVOne_thenFalse() {
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
    void givenRespondentUnrepresentedAndApplicantUnrepresentedAndOnevOne_whenIsLipvLipOneVOne_thenTrue() {
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
    void givenApplicantUnrepresented_whenIsApplicant1NotRepresented_thenTrue() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1Represented(YesOrNo.NO)
            .applicant1(Party.builder().build())
            .build();
        //Then
        assertTrue(caseData.isApplicant1NotRepresented());
    }

    @Test
    void givenApplicantRepresented_whenIsApplicant1NotRepresented_thenFalse() {
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
            .responseClaimTrack(FAST_CLAIM.name())
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
        assertEquals(SDO_ORDER, caseDocument.get().getValue().getDocumentType());
    }

    @Test
    void getSDOOrderDocument_shouldReturnLatest_WhenItPresent() {
        CaseData caseData = CaseData.builder()
            .systemGeneratedCaseDocuments(wrapElements(
                CaseDocument.builder().documentType(SDO_ORDER)
                    .createdDatetime(LocalDateTime.now().minusDays(2)).documentName("Doc1").build(),
                CaseDocument.builder().documentType(SDO_ORDER)
                    .createdDatetime(LocalDateTime.now().minusDays(1)).documentName("Doc2").build()
            )).build();
        //When
        Optional<Element<CaseDocument>> caseDocument = caseData.getSDODocument();
        //Then
        assertEquals(SDO_ORDER, caseDocument.get().getValue().getDocumentType());
        assertEquals("Doc2", caseDocument.get().getValue().getDocumentName());
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
    void shouldReturnTrueWhenBilingual() {

        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .build();
        caseData.setClaimantBilingualLanguagePreference("BOTH");

        //When
        boolean result = caseData.isClaimantBilingual();

        //Then
        assertTrue(result);
    }

    @Test
    void shouldReturnTrueWhenRespondentSignSettlementAgreementIsNotNull() {

        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .caseDataLip(CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.NO).build())
            .build();

        //When
        boolean isRespondentSignSettlementAgreement = caseData.isRespondentRespondedToSettlementAgreement();

        //Then
        assertTrue(isRespondentSignSettlementAgreement);
    }

    @Test
    void shouldReturnFalseWhenRespondentSignSettlementAgreementIsNull() {

        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .caseDataLip(CaseDataLiP.builder().build())
            .build();

        //When
        boolean isRespondentSignSettlementAgreement = caseData.isRespondentRespondedToSettlementAgreement();

        //Then
        assertFalse(isRespondentSignSettlementAgreement);
    }

    @Test
    void isSignSettlementAgreementDeadlineNotExpired_thenFalse() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStatePriorToRespondToSettlementAgreementDeadline().build();
        //When
        //Then
        assertThat(caseData.isSettlementAgreementDeadlineExpired()).isFalse();
    }

    @Test
    void isSignSettlementAgreementDeadlineExpired_thenTrue() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStatePastRespondToSettlementAgreementDeadline().build();
        //When
        //Then
        assertThat(caseData.isSettlementAgreementDeadlineExpired()).isTrue();
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

    @Test
    void shouldReturnNull_whenNoClaimFeePresent() {
        //Given
        CaseData caseData = CaseData.builder().build();
        //When
        BigDecimal claimAmount = caseData.getClaimAmountInPounds();
        //Then
        assertThat(claimAmount).isNull();
    }

    @Test
    void shouldReturnClaimValueInPounds_whenClaimValuePresent() {
        //Given
        CaseData caseData = CaseData.builder()
            .claimValue(ClaimValue
                            .builder()
                            .statementOfValueInPennies(new BigDecimal(1000))
                            .build())
            .build();
        //When
        BigDecimal claimAmount = caseData.getClaimAmountInPounds();
        //Then
        assertThat(claimAmount).isEqualTo(new BigDecimal(10).setScale(2));
    }

    @Test
    void shouldReturnClaimValueInPounds_whenTotalClaimAmountPresent() {
        //Given
        CaseData caseData = CaseData.builder()
            .totalClaimAmount(new BigDecimal(1000))
            .build();
        //When
        BigDecimal claimAmount = caseData.getClaimAmountInPounds();
        //Then
        assertThat(claimAmount).isEqualTo(new BigDecimal(1000).setScale(2));
    }

    @Test
    void shouldReturnClaimValueInPounds_whenTotalClaimAmountAndInterestPresent() {
        //Given
        CaseData caseData = CaseData.builder()
            .totalClaimAmount(new BigDecimal(1000))
            .totalInterest(new BigDecimal(10))
            .build();
        //When
        BigDecimal claimAmount = caseData.getClaimAmountInPounds();
        //Then
        assertThat(claimAmount).isEqualTo(new BigDecimal(1010).setScale(2));
    }

    @Test
    void shouldReturnTrue_whenRespondent2NotRespresentedUnspec() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent2Represented(NO)
            .build();
        //When
        boolean actual = caseData.isRespondent2NotRepresented();
        //Then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnTrue_whenRespondent2NotRespresentedSpec() {
        //Given
        CaseData caseData = CaseData.builder()
            .specRespondent2Represented(NO)
            .build();
        //When
        boolean actual = caseData.isRespondent2NotRepresented();
        //Then
        assertThat(actual).isTrue();
    }

    @Test
    void shouldReturnFalse_whenRespondent2RespresentedUnspec() {
        //Given
        CaseData caseData = CaseData.builder()
            .respondent2Represented(YES)
            .build();
        //When
        boolean actual = caseData.isRespondent2NotRepresented();
        //Then
        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnFalse_whenRespondent2RespresentedSpec() {
        //Given
        CaseData caseData = CaseData.builder()
            .specRespondent2Represented(YES)
            .build();
        //When
        boolean actual = caseData.isRespondent2NotRepresented();
        //Then
        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnFalse_whenRespondent2RespresentedNullUnspec() {
        //Given
        CaseData caseData = CaseData.builder()
            .build();
        //When
        boolean actual = caseData.isRespondent2NotRepresented();
        //Then
        assertThat(actual).isFalse();
    }

    @Test
    void shouldReturnFalse_whenRespondent2RespresentedNullSpec() {
        //Given
        CaseData caseData = CaseData.builder().build();
        //When
        boolean actual = caseData.isRespondent2NotRepresented();
        //Then
        assertThat(actual).isFalse();
    }

    @Nested
    class GetHearingLocationText {

        @Test
        void shouldReturnNull_whenHearingLocationIsNull() {
            CaseData caseData = CaseData.builder().build();
            String actual = caseData.getHearingLocationText();

            assertNull(actual);
        }

        @Test
        void shouldReturnNull_whenHearingLocationValueIsNull() {
            CaseData caseData = CaseData.builder()
                .hearingLocation(DynamicList.builder().value(DynamicListElement.EMPTY).build()).build();
            String actual = caseData.getHearingLocationText();

            assertNull(actual);
        }

        @Test
        void shouldExpectedString_whenHearingLocationValueLabelIsNotNull() {
            CaseData caseData = CaseData.builder()
                .hearingLocation(DynamicList.builder().value(
                    DynamicListElement.dynamicElement("label")).build()).build();
            String actual = caseData.getHearingLocationText();

            assertEquals("label", actual);
        }

        @Test
        void shouldReturnTrueWhenRespondentSignSettlementAgreementIsNotNull() {

            //Given
            CaseData caseData = CaseDataBuilder.builder()
                .caseDataLip(CaseDataLiP.builder().respondentSignSettlementAgreement(YesOrNo.NO).build())
                .build();

            //When
            boolean isRespondentSignSettlementAgreement = caseData.isRespondentRespondedToSettlementAgreement();

            //Then
            assertTrue(isRespondentSignSettlementAgreement);
        }
    }

    @Nested
    class JudgementByAdmissionConditions {

        @Test
        void shouldReturnTrueWhenWillThisAmountBePaidIsAfterCurrentDateAndFAPayImmediately() {
            //Given
            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
                .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                                                   .whenWillThisAmountBePaid(LocalDate.now().plusDays(1)).build())
                .build();
            //When
            boolean isJudgementDateNotPermitted = caseData.isJudgementDateNotPermitted();
            //Then
            assertTrue(isJudgementDateNotPermitted);
        }

        @Test
        void shouldReturnTrueWhenFirstRepaymentDateIsAfterCurrentDateAndDefendantAcceptsSettlementAgreement() {
            //Given
            CaseDataLiP caseDataLiP = CaseDataLiP.builder()
                .respondentSignSettlementAgreement(YesOrNo.YES).build();

            CaseData caseData = CaseData.builder()
                .caseDataLiP(caseDataLiP)
                .respondent1RepaymentPlan(RepaymentPlanLRspec.builder()
                                              .firstRepaymentDate(LocalDate.now().plusDays(1)).build())
                .build();
            //When
            boolean isJudgementDateNotPermitted = caseData.isJudgementDateNotPermitted();
            //Then
            assertTrue(isJudgementDateNotPermitted);
        }

        @Test
        void shouldReturnTrueWhenSignSettlementAgreementDeadlineIsAfterCurrentDate() {
            //Given

            CaseData caseData = CaseData.builder()
                .respondent1RespondToSettlementAgreementDeadline(LocalDateTime.now().plusDays(1))
                .respondent1RepaymentPlan(RepaymentPlanLRspec.builder()
                                              .firstRepaymentDate(LocalDate.now().plusDays(3)).build())
                .build();
            //When
            boolean isJudgementDateNotPermitted = caseData.isJudgementDateNotPermitted();
            //Then
            assertTrue(isJudgementDateNotPermitted);
        }

        @Test
        void shouldReturnFalseWhenSignSettlementAgreementDeadlineIsBeforeCurrentDate() {
            //Given

            CaseData caseData = CaseData.builder()
                .respondent1RespondToSettlementAgreementDeadline(LocalDateTime.now().minusDays(1))
                .respondent1RepaymentPlan(RepaymentPlanLRspec.builder()
                                              .firstRepaymentDate(LocalDate.now().plusDays(3)).build())
                .build();
            //When
            boolean isJudgementDateNotPermitted = caseData.isJudgementDateNotPermitted();
            //Then
            assertFalse(isJudgementDateNotPermitted);
        }

        @Test
        void shouldReturnFalseWhenSignSettlementAgreementIsRejectedByDefendant() {
            //Given
            CaseDataLiP caseDataLiP = CaseDataLiP.builder()
                .respondentSignSettlementAgreement(YesOrNo.NO).build();

            CaseData caseData = CaseData.builder()
                .caseDataLiP(caseDataLiP)
                .respondent1RepaymentPlan(RepaymentPlanLRspec.builder()
                                              .firstRepaymentDate(LocalDate.now().plusDays(1)).build())
                .build();
            //When
            boolean isJudgementDateNotPermitted = caseData.isJudgementDateNotPermitted();
            //Then
            assertFalse(isJudgementDateNotPermitted);
        }

        @Test
        void shouldReturnFalseWhenFirstRepaymentDateIsBeforeCurrentDateAndDefendantAcceptsSettlementAgreement() {
            //Given
            CaseDataLiP caseDataLiP = CaseDataLiP.builder()
                .respondentSignSettlementAgreement(YesOrNo.YES).build();

            CaseData caseData = CaseData.builder()
                .caseDataLiP(caseDataLiP)
                .respondent1RepaymentPlan(RepaymentPlanLRspec.builder()
                                              .firstRepaymentDate(LocalDate.now().minusDays(1)).build())
                .build();
            //When
            boolean isJudgementDateNotPermitted = caseData.isJudgementDateNotPermitted();
            //Then
            assertFalse(isJudgementDateNotPermitted);
        }

        @Test
        void shouldReturnFalseWhenWillThisAmountBePaidIsBeforeCurrentDateAndDefendantAcceptsSettlementAgreement() {
            //Given
            CaseDataLiP caseDataLiP = CaseDataLiP.builder()
                .respondentSignSettlementAgreement(YesOrNo.YES).build();

            CaseData caseData = CaseData.builder()
                .caseDataLiP(caseDataLiP)
                .respondToClaimAdmitPartLRspec(RespondToClaimAdmitPartLRspec.builder()
                                                   .whenWillThisAmountBePaid(LocalDate.now().minusDays(1)).build())
                .build();
            //When
            boolean isJudgementDateNotPermitted = caseData.isJudgementDateNotPermitted();
            //Then
            assertFalse(isJudgementDateNotPermitted);
        }

        @Test
        void shouldReturnTrueWhenBothDatesAreNull() {
            //Given
            CaseData caseData = CaseData.builder()
                .build();
            //When
            boolean isJudgementDateNotPermitted = caseData.isJudgementDateNotPermitted();
            //Then
            assertTrue(isJudgementDateNotPermitted);
        }

        @Test
        void shouldReturnTrueWhenCourtFavoursClaimantAndSetDateIsAfterCurrentDateAndSettlementAgreementSigned() {
            //Given
            CaseData caseData = CaseData.builder()
                .applicant1RepaymentOptionForDefendantSpec(PaymentType.SET_DATE)
                .applicant1RequestedPaymentDateForDefendantSpec(PaymentBySetDate.builder()
                                                                    .paymentSetDate(LocalDate.now().plusDays(1)).build())
                .caseDataLiP(CaseDataLiP.builder()
                                 .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                            .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT).build())
                                 .respondentSignSettlementAgreement(YES).build())
                .build();
            //When
            boolean isJudgementDateNotPermitted = caseData.isJudgementDateNotPermitted();
            //Then
            assertTrue(isJudgementDateNotPermitted);
        }

        @Test
        void shouldReturnTrueWhenCourtFavoursClaimantAndPayImmediatelyDateIsAfterCurrentDateAndSettlementAgreementSigned() {
            //Given
            CaseData caseData = CaseData.builder()
                .applicant1RepaymentOptionForDefendantSpec(PaymentType.IMMEDIATELY)
                .applicant1SuggestPayImmediatelyPaymentDateForDefendantSpec(LocalDate.now().plusDays(1))
                .caseDataLiP(CaseDataLiP.builder()
                                 .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                            .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT).build())
                                 .respondentSignSettlementAgreement(YES).build())
                .build();
            //When
            boolean isJudgementDateNotPermitted = caseData.isJudgementDateNotPermitted();
            //Then
            assertTrue(isJudgementDateNotPermitted);
        }
    }

    @Nested
    class GetAssignedTrack {

        @Test
        void shouldReturnExpectedAssignedTrack_whenAllocatedTrackIsDefined() {
            CaseData caseData = CaseData.builder().allocatedTrack(FAST_CLAIM).build();
            assertEquals(FAST_CLAIM.name(), caseData.getAssignedTrack());
        }

        @Test
        void shouldReturnExpectedAssignedTrack_whenResponseClaimTrackIsDefined() {
            CaseData caseData = CaseData.builder().responseClaimTrack(MULTI_CLAIM.name()).build();
            assertEquals(MULTI_CLAIM.name(), caseData.getAssignedTrack());
        }
    }

    @Nested
    class HWFType {
        @Test
        void shouldReturnTrueIfHWFTypeIsHearing() {
            //Given
            CaseData caseData = CaseData.builder()
                .hwfFeeType(FeeType.HEARING)
                .build();
            //When
            boolean isHWFTypeHearing = caseData.isHWFTypeHearing();
            //Then
            assertTrue(isHWFTypeHearing);
        }

        @Test
        void shouldReturnFalseIfHWFTypeIsNull() {
            //Given
            CaseData caseData = CaseData.builder()
                .build();
            //When
            boolean isHWFTypeHearing = caseData.isHWFTypeHearing();
            //Then
            assertFalse(isHWFTypeHearing);
        }

        @Test
        void shouldReturnTrueIfHWFTypeIsClaimIssued() {
            //Given
            CaseData caseData = CaseData.builder()
                .hwfFeeType(FeeType.CLAIMISSUED)
                .build();
            //When
            boolean isHWFTypeClaimIssued = caseData.isHWFTypeClaimIssued();
            //Then
            assertTrue(isHWFTypeClaimIssued);
        }

        @Test
        void shouldReturnFalseIfHWFTypeIsNotClaimIssued() {
            //Given
            CaseData caseData = CaseData.builder()
                .hwfFeeType(FeeType.HEARING)
                .build();
            //When
            boolean isHWFTypeClaimIssued = caseData.isHWFTypeClaimIssued();
            //Then
            assertFalse(isHWFTypeClaimIssued);
        }
    }

    @Nested
    class CoSC {
        @Test
        void shouldReturnTrue_CoscCertExists() {
            CaseDocument caseDocument = CaseDocument.builder()
                .documentType(DocumentType.CERTIFICATE_OF_DEBT_PAYMENT)
                .build();
            CaseData caseData = CaseData.builder()
                .systemGeneratedCaseDocuments(wrapElements(caseDocument))
                .build();
            assertTrue(caseData.hasCoscCert());
        }

        @Test
        void shouldReturnFalse_CoscCertDoesntExists() {
            CaseData caseData = CaseData.builder().build();
            assertFalse(caseData.hasCoscCert());
        }
    }

    @Nested
    class AlreadyPaidAmountCheck {
        @Test
        void shouldReturnTrueIfPaidLessFullDefence() {
            //Given
            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
                .respondToClaim(RespondToClaim.builder()
                                    .howMuchWasPaid(new BigDecimal(1000))
                                    .build())
                .totalClaimAmount(new BigDecimal(1000))
                .build();
            //When
            boolean isPaidLessThanClaimAmount = caseData.isPaidLessThanClaimAmount();
            //Then
            assertTrue(isPaidLessThanClaimAmount);
        }

        @Test
        void shouldReturnTrueIfPaidLessPartAdmit() {
            //Given
            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(PART_ADMISSION)
                .respondToAdmittedClaim(RespondToClaim.builder()
                                            .howMuchWasPaid(new BigDecimal(1000))
                                            .build())
                .totalClaimAmount(new BigDecimal(1000))
                .build();
            //When
            boolean isPaidLessThanClaimAmount = caseData.isPaidLessThanClaimAmount();
            //Then
            assertTrue(isPaidLessThanClaimAmount);
        }

        @Test
        void shouldReturnFalseIfFullAdmission() {
            //Given
            CaseData caseData = CaseData.builder()
                .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
                .totalClaimAmount(new BigDecimal(1000))
                .build();
            //When
            boolean isPaidLessThanClaimAmount = caseData.isPaidLessThanClaimAmount();
            //Then
            assertFalse(isPaidLessThanClaimAmount);
        }

        @Test
        void shouldReturnTrueIfHearingFeePaymentStatusSuccess() {
            //Given
            CaseData caseData = CaseData.builder()
                .hearingFeePaymentDetails(PaymentDetails.builder().status(SUCCESS).build())
                .build();
            //When
            boolean isHearingFeePaid = caseData.isHearingFeePaid();
            //Then
            assertTrue(isHearingFeePaid);
        }

        @Test
        void shouldReturnTrueIfHearingFeePaidWithHWF() {
            //Given
            CaseData caseData = CaseData.builder()
                .hearingHelpFeesReferenceNumber("hwf-ref")
                .feePaymentOutcomeDetails(
                    FeePaymentOutcomeDetails.builder()
                        .hwfFullRemissionGrantedForHearingFee(YES)
                        .build())
                .applicant1Represented(NO)
                .respondent1Represented(NO)
                .build();
            //When
            boolean isHearingFeePaid = caseData.isHearingFeePaid();
            //Then
            assertTrue(isHearingFeePaid);
        }

        @Test
        void shouldReturnFalseIfHearingPaymentIsNotSuccess() {
            //Given
            CaseData caseData = CaseData.builder()
                .hearingFeePaymentDetails(PaymentDetails.builder().status(FAILED).build())
                .build();
            //When
            boolean isHearingFeePaid = caseData.isHearingFeePaid();
            //Then
            assertFalse(isHearingFeePaid);
        }

        @Test
        void shouldReturnFalseIfHearingPaymentIsNullAndNoHWF() {
            //Given
            CaseData caseData = CaseData.builder().build();
            //When
            boolean isHearingFeePaid = caseData.isHearingFeePaid();
            //Then
            assertFalse(isHearingFeePaid);
        }
    }

    @ParameterizedTest
    @MethodSource("provideDocumentListTestData")
    void shouldReturnExpectedDocumentList(DocumentType documentType, List<Element<CaseDocument>> documentCollection, Optional<List<CaseDocument>> expected) {
        CaseData caseData = CaseData.builder()
            .systemGeneratedCaseDocuments(documentCollection)
            .finalOrderDocumentCollection(documentCollection)
            .build();

        List<Element<CaseDocument>> documents = DocumentType.SDO_ORDER.equals(documentType)
            ? caseData.getSystemGeneratedCaseDocuments()
            : caseData.getFinalOrderDocumentCollection();

        Optional<List<CaseDocument>> result = caseData.getDocumentListByType(documents, documentType);
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> provideDocumentListTestData() {
        return Stream.of(
            Arguments.of(DocumentType.SDO_ORDER, new ArrayList<>(), Optional.empty()),
            Arguments.of(DocumentType.SDO_ORDER,
                         List.of(ElementUtils.element(CaseDocument.builder().documentType(DocumentType.DEFENCE_TRANSLATED_DOCUMENT).build())),
                         Optional.empty()
            ),
            Arguments.of(DocumentType.SDO_ORDER,
                         List.of(
                             ElementUtils.element(CaseDocument.builder().documentType(DocumentType.SDO_ORDER).build()),
                             ElementUtils.element(CaseDocument.builder().documentType(DocumentType.DEFENCE_TRANSLATED_DOCUMENT).build())
                         ),
                         Optional.of(List.of(CaseDocument.builder().documentType(DocumentType.SDO_ORDER).build()))
            ),
            Arguments.of(DocumentType.JUDGE_FINAL_ORDER,
                         List.of(
                             ElementUtils.element(CaseDocument.builder().documentType(DocumentType.JUDGE_FINAL_ORDER).build())
                         ),
                         Optional.of(List.of(CaseDocument.builder().documentType(DocumentType.JUDGE_FINAL_ORDER).build()))
            ),
            Arguments.of(DocumentType.JUDGE_FINAL_ORDER,
                         List.of(
                             ElementUtils.element(CaseDocument.builder().documentType(DocumentType.JUDGE_FINAL_ORDER).build()),
                             ElementUtils.element(CaseDocument.builder().documentType(DocumentType.DEFENCE_TRANSLATED_DOCUMENT).build())
                         ),
                         Optional.of(List.of(CaseDocument.builder().documentType(DocumentType.JUDGE_FINAL_ORDER).build()))
            )
        );
    }

    @Test
    void shouldReturnNullForDefaultObligationWAFlag() {
        // Given
        CaseData caseData = CaseData.builder().build();

        // When
        ObligationWAFlag obligationWAFlag = caseData.getObligationWAFlag();

        // Then
        assertNull(obligationWAFlag);
    }

    @Test
    void shouldSetAndReturnObligationWAFlag() {
        // Given
        ObligationWAFlag expectedFlag = new ObligationWAFlag("Test", "Test", "1 January 2024");
        CaseData caseData = CaseData.builder().obligationWAFlag(expectedFlag).build();

        // When
        ObligationWAFlag obligationWAFlag = caseData.getObligationWAFlag();

        // Then
        assertEquals(expectedFlag, obligationWAFlag);
    }

    @Test
    void shouldReturnNullWhenApplicantSolicitor1UserDetailsEmailIsNull() {
        CaseData caseData = CaseData.builder().build();
        assertNull(caseData.getApplicantSolicitor1UserDetailsEmail());
    }

    @Test
    void shouldReturnApplicantSolicitor1UserDetailsEmail() {
        CaseData caseData = CaseData.builder().applicantSolicitor1UserDetails(
            IdamUserDetails.builder().email("test@test.com").build()
        ).build();
        assertEquals("test@test.com", caseData.getApplicantSolicitor1UserDetailsEmail());
    }

    @Test
    void shouldReturnNullEmailWhenApplicantSolicitor1UserDetailsIsNull() {
        CaseData caseData = CaseData.builder().build();
        assertNull(caseData.getApplicantSolicitor1UserDetailsEmail());
    }

    @Test
    void shouldReturnClaimantUserDetailsEmail() {
        CaseData caseData = CaseData.builder().claimantUserDetails(
            IdamUserDetails.builder().email("test@test.com").build()
        ).build();
        assertEquals("test@test.com", caseData.getClaimantUserDetailsEmail());
    }

    @Test
    void shouldReturnNullEmailWhenClaimantUserDetailsIsNull() {
        CaseData caseData = CaseData.builder().build();
        assertNull(caseData.getClaimantUserDetailsEmail());
    }

    @Test
    void shouldReturnRespondent1PartyEmail() {
        CaseData caseData = CaseData.builder().respondent1(
            Party.builder().partyEmail("test@test.com").build()
        ).build();
        assertEquals("test@test.com", caseData.getRespondent1PartyEmail());
    }

    @Test
    void shouldReturnNullEmailWhenRespondent1PartyIsNull() {
        CaseData caseData = CaseData.builder().build();
        assertNull(caseData.getRespondent1PartyEmail());
    }

    @Test
    void shouldReturnRespondent2PartyEmail() {
        CaseData caseData = CaseData.builder().respondent1(
            Party.builder().partyEmail("test@test.com").build()
        ).build();
        assertEquals("test@test.com", caseData.getRespondent1PartyEmail());
    }

    @Test
    void shouldReturnNullEmailWhenRespondent2PartyIsNull() {
        CaseData caseData = CaseData.builder().build();
        assertNull(caseData.getRespondent1PartyEmail());
    }
}


