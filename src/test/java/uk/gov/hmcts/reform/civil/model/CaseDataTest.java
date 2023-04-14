package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
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
    void givenNotOneVTwoTowLegalRepCaseResponsePartAdmit_whenIsRespondentResponseFullDefence_thenFalse() {
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
}
