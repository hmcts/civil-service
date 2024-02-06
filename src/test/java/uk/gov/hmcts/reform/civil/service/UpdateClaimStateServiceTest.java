package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.Party.Type.COMPANY;

@ExtendWith(MockitoExtension.class)
class UpdateClaimStateServiceTest {

    @InjectMocks
    private UpdateClaimStateService service;

    @Test
    void shouldUpdateCaseStateToJudicialReferral_WhenPartAdmitNoSettle_NoMediation() {
        //Given
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                                                         .hasAgreedFreeMediation(MediationDecision.No).build())
            .build();
        CaseData caseData = CaseDataBuilder.builder()
            .caseDataLip(caseDataLiP)
            .applicant1AcceptAdmitAmountPaidSpec(NO)
            .atStateClaimIssued().build();

        //When
        var response = service.setUpCaseState(caseData);

        //Then
        assertEquals(CaseState.JUDICIAL_REFERRAL.name(), response);

    }

    @Test
    void shouldUpdateCaseStateToJudicialReferral_WhenNotReceivedPayment_NoMediation_ForPartAdmit() {
        //Given
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                                                         .hasAgreedFreeMediation(MediationDecision.No).build())
            .build();
        CaseData caseData = CaseDataBuilder.builder()
            .caseDataLip(caseDataLiP)
            .applicant1PartAdmitConfirmAmountPaidSpec(NO)
            .atStateClaimIssued().build();

        //When
        var response = service.setUpCaseState(caseData);

        //Then
        assertEquals(CaseState.JUDICIAL_REFERRAL.name(), response);

    }

    @Test
    void shouldUpdateCaseStateToJudicialReferral_WhenFullDefence_NotPaid_NoMediation() {
        //Given
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                                                         .hasAgreedFreeMediation(MediationDecision.No).build())
            .build();
        CaseData caseData =
            CaseDataBuilder.builder().caseDataLip(caseDataLiP).applicant1PartAdmitIntentionToSettleClaimSpec(NO)
                .atStateClaimIssued()
                .build();

        //When
        var response = service.setUpCaseState(caseData);

        //Then
        assertEquals(CaseState.JUDICIAL_REFERRAL.name(), response);

    }

    @Test
    void shouldUpdateCaseStateToJudicialReferral_WhenFullDefence_NotPaid_FastTrack() {
        //Given
        CaseData caseData =
            CaseDataBuilder.builder().applicant1PartAdmitIntentionToSettleClaimSpec(NO)
                .responseClaimTrack(FAST_CLAIM.name())
                .atStateClaimIssued()
                .build();
        //When
        var response = service.setUpCaseState(caseData);
        //Then
        assertEquals(CaseState.JUDICIAL_REFERRAL.name(), response);

    }

    @Test
    void shouldUpdateCaseStateToJudicialReferral_WhenFullDefence() {
        //Given
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                                                         .hasAgreedFreeMediation(MediationDecision.No).build())
            .build();
        CaseData caseData =
            CaseDataBuilder.builder().caseDataLip(caseDataLiP).applicant1ProceedWithClaim(YES)
                .atStateClaimIssued()
                .build();
        //When
        var response = service.setUpCaseState(caseData);
        //Then
        assertEquals(CaseState.JUDICIAL_REFERRAL.name(), response);
    }

    @Test
    void shouldUpdateCaseStateToCaseDismissed_WhenFullDefence_FastTrack() {
        //Given
        CaseData caseData =
            CaseDataBuilder.builder().applicant1ProceedWithClaim(NO)
                .responseClaimTrack(FAST_CLAIM.name())
                .atStateClaimIssued()
                .build();
        //When
        var response = service.setUpCaseState(caseData);
        //Then
        assertEquals(CaseState.CASE_DISMISSED.name(), response);
    }

    @Test
    void shouldChangeCaseState_whenApplicantRejectClaimSettlementAndAgreeToMediation() {
        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .applicant1PartAdmitConfirmAmountPaidSpec(NO)
            .caseDataLip(CaseDataLiP.builder().applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder().hasAgreedFreeMediation(
                    MediationDecision.Yes).build())
                             .build())
            .build().toBuilder()
            .responseClaimMediationSpecRequired(YES).build();

        //When
        var response = service.setUpCaseState(caseData);
        //Then
        assertThat(response).isEqualTo(CaseState.IN_MEDIATION.name());
    }

    @Test
    void shouldChangeCaseState_whenApplicantAcceptRepaymentPlanAndChooseSettlementAgreement() {
        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES)
            .caseDataLip(CaseDataLiP.builder().applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                                         .applicant1SignedSettlementAgreement(
                                                                             YesOrNo.YES).build())
                             .build())
            .build();

        //When
        var response = service.setUpCaseState(caseData);

        //Then
        assertThat(response).isEqualTo(CaseState.All_FINAL_ORDERS_ISSUED.name());
    }

    @Test
    void shouldChangeCaseState_whenApplicantAgreeClaimSettlement() {
        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .applicant1PartAdmitIntentionToSettleClaimSpec(YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YES)
            .build().toBuilder()
            .responseClaimMediationSpecRequired(YES).build();

        //When
        var response = service.setUpCaseState(caseData);
        //Then
        assertThat(response).isEqualTo(CaseState.CASE_SETTLED.name());
    }

    @Test
    void shouldChangeCaseState_whenApplicantRejectRepaymentPlanAndIsCompany_toAllFinalOrdersIssued() {
        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1AcceptPartAdmitPaymentPlanSpec(NO)
            .caseDataLip(CaseDataLiP.builder().applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder().hasAgreedFreeMediation(
                MediationDecision.No).build()).build())
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName("CLAIMANT_ORG_NAME").build())
            .respondent1(Party.builder()
                             .type(COMPANY)
                             .companyName("Test Inc")
                             .build())
            .build();

        //When
        var response = service.setUpCaseState(caseData);

        //Then
        assertThat(response).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
    }

    @Test
    void shouldChangeCaseState_whenApplicantAcceptedPartAdmitImmediatePayment() {
        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1AcceptAdmitAmountPaidSpec(YES)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.IMMEDIATELY)
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName("CLAIMANT_ORG_NAME").build())
            .respondent1(Party.builder()
                             .type(COMPANY)
                             .companyName("Test Inc")
                             .build())
            .build();

        //When
        var response = service.setUpCaseState(caseData);

        //Then
        assertThat(response).isEqualTo(CaseState.CASE_SETTLED.name());
    }

    @Test
    void shouldChangeCaseState_whenApplicantRejectedRepaymentPlanAndRequestCCJ_CourtAcceptsClaimantDecision_ForPartAmit() {
        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build())
            .caseDataLip(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .build()).build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("CLAIMANT_NAME")
                             .build())
            .build();

        //When
        var response = service.setUpCaseState(caseData);

        //Then
        assertThat(response).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
    }

    @Test
    void shouldChangeCaseState_whenApplicantRejectedRepaymentPlanAndRequestCCJ_RejectedManualDetermination_ForPartAmit_PayBySetDate() {
        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build())
            .caseDataLip(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .claimantResponseOnCourtDecision(
                                                            ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_DATE
                                                        )
                                                        .build()).build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("CLAIMANT_NAME")
                             .build())
            .build();

        //When
        var response = service.setUpCaseState(caseData);

        //Then
        assertThat(response).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
    }

    @Test
    void shouldChangeCaseState_whenApplicantRejectedRepaymentPlanAndRequestCCJ_AcceptManualDetermination_ForPartAmit_ForPayByInstalments() {
        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build())
            .caseDataLip(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .claimantResponseOnCourtDecision(
                                                            ClaimantResponseOnCourtDecisionType.ACCEPT_REPAYMENT_PLAN
                                                        )
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .build()).build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("CLAIMANT_NAME")
                             .build())
            .build();

        //When
        var response = service.setUpCaseState(caseData);

        //Then
        assertThat(response).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
    }

    @Test
    void shouldChangeCaseState_whenApplicantRejectedRepaymentPlanAndRequestCCJ_AcceptManualDetermination_ForPartAmit_ForPayBySetDate() {
        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build())
            .caseDataLip(CaseDataLiP.builder()
                             .applicant1LiPResponse(ClaimantLiPResponse.builder()
                                                        .claimantResponseOnCourtDecision(
                                                            ClaimantResponseOnCourtDecisionType.ACCEPT_REPAYMENT_DATE
                                                        )
                                                        .applicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ)
                                                        .build()).build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("CLAIMANT_NAME")
                             .build())
            .build();

        //When
        var response = service.setUpCaseState(caseData);

        //Then
        assertThat(response).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
    }
}
