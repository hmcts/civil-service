package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ChooseHowToProceed;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.INTERMEDIATE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.Party.Type.COMPANY;

@ExtendWith(MockitoExtension.class)
class UpdateClaimStateServiceTest {

    @InjectMocks
    private UpdateClaimStateService service;

    @Mock
    FeatureToggleService featureToggleService;

    @BeforeEach
    void before() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(false);
    }

    @Test
    void shouldNotUpdateCaseState_WhenMultiClaimClaimantLiP() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CaseData caseData =
            CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .applicant1ProceedWithClaim(YES)
                .applicant1PartAdmitIntentionToSettleClaimSpec(NO)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .build().toBuilder()
                .applicant1Represented(NO)
                .responseClaimTrack(MULTI_CLAIM.name())
                .build();
        String actualState = service.setUpCaseState(caseData);

        assertEquals(CaseState.AWAITING_APPLICANT_INTENTION.name(), actualState);
    }

    @Test
    void shouldNotUpdateCaseState_WhenMultiClaimRespondentLiP() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CaseData caseData =
            CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .applicant1ProceedWithClaim(YES)
                .applicant1PartAdmitIntentionToSettleClaimSpec(NO)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .build().toBuilder()
                .respondent1Represented(NO)
                .specRespondent1Represented(NO)
                .responseClaimTrack(MULTI_CLAIM.name())
                .build();
        String actualState = service.setUpCaseState(caseData);

        assertEquals(CaseState.AWAITING_APPLICANT_INTENTION.name(), actualState);
    }

    @Test
    void shouldNotUpdateCaseState_WhenIntermediateClaimClaimantLiP() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CaseData caseData =
            CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .applicant1ProceedWithClaim(YES)
                .applicant1PartAdmitIntentionToSettleClaimSpec(NO)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .build().toBuilder()
                .applicant1Represented(NO)
                .responseClaimTrack(INTERMEDIATE_CLAIM.name())
                .build();
        String actualState = service.setUpCaseState(caseData);

        assertEquals(CaseState.AWAITING_APPLICANT_INTENTION.name(), actualState);
    }

    @Test
    void shouldNotUpdateCaseState_WhenIntermediateClaimRespondentLiP() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CaseData caseData =
            CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .applicant1ProceedWithClaim(YES)
                .applicant1PartAdmitIntentionToSettleClaimSpec(NO)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .build().toBuilder()
                .specRespondent1Represented(NO)
                .respondent1Represented(NO)
                .responseClaimTrack(INTERMEDIATE_CLAIM.name())
                .build();
        String actualState = service.setUpCaseState(caseData);

        assertEquals(CaseState.AWAITING_APPLICANT_INTENTION.name(), actualState);
    }

    @Test
    void shouldUpdateCaseStateToInMediation_WhenSmallClaimCarmEnabled1v1() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                                                         .hasAgreedFreeMediation(MediationDecision.No).build())
            .build();
        CaseData caseData =
            CaseDataBuilder.builder().caseDataLip(caseDataLiP)
                .applicant1ProceedWithClaim(YES)
                .applicant1PartAdmitIntentionToSettleClaimSpec(NO)
                .atStateClaimIssued()
                .build().toBuilder()
                .responseClaimTrack(SMALL_CLAIM.name())
                .build();
        String actualState = service.setUpCaseState(caseData);

        assertEquals(CaseState.IN_MEDIATION.name(), actualState);
    }

    @Test
    void shouldUpdateCaseStateToJudicialReferral_WhenSmallClaimCarmNotEnabled() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                                                         .hasAgreedFreeMediation(MediationDecision.No).build())
            .build();
        CaseData caseData =
            CaseDataBuilder.builder().caseDataLip(caseDataLiP)
                .applicant1ProceedWithClaim(YES)
                .applicant1PartAdmitIntentionToSettleClaimSpec(NO)
                .atStateClaimIssued()
                .build().toBuilder()
                .responseClaimTrack(SMALL_CLAIM.name())
                .build();

        String actualState = service.setUpCaseState(caseData);

        assertEquals(CaseState.JUDICIAL_REFERRAL.name(), actualState);
    }

    @Test
    void shouldUpdateCaseStateToJudicialReferral_WhenPartAdmitNoSettle_NoMediation() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
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
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
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
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
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
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
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
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
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
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        //Given
        CaseData caseData =
            CaseDataBuilder.builder().applicant1ProceedWithClaim(NO)
                .responseClaimTrack(FAST_CLAIM.name())
                .atStateClaimIssued()
                .build();
        //When
        var response = service.setUpCaseState(caseData);
        //Then
        assertEquals(CaseState.CASE_STAYED.name(), response);
    }

    @Test
    void shouldChangeCaseState_whenApplicantRejectClaimSettlementAndAgreeToMediation() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
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
    void shouldChangeCaseState_whenApplicantAgreeClaimSettlement() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
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
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
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
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .build().toBuilder()
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
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
        assertThat(response).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());
    }

    @Test
    void shouldChangeCaseState_whenApplicantRejectedRepaymentPlanAndRequestCCJ_CourtAcceptsClaimantDecision_ForPartAmit() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
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
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
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
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
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
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
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

    @Test
    void shouldUpdateCaseStateToAllFinalOrderIssued_whenApplicantAcceptOrRejectedRepaymentPlanAndRequestCCJ_JudgementOnlineLiveEnabled() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1ResponseDate(LocalDateTime.now())
            .applicant1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("CLAIMANT_NAME").build())
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("DEFENDANT_NAME").build())
            .respondent1Represented(NO)
            .specRespondent1Represented(NO)
            .applicant1Represented(NO)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YES)
            .ccjPaymentDetails(CCJPaymentDetails.builder()
                                   .ccjPaymentPaidSomeOption(YES)
                                   .ccjJudgmentFixedCostAmount(BigDecimal.valueOf(10))
                                   .ccjJudgmentTotalStillOwed(BigDecimal.valueOf(150))
                                   .build())
            .build();
        String actualState = service.setUpCaseState(caseData);

        assertEquals(CaseState.All_FINAL_ORDERS_ISSUED.name(), actualState);
    }
}
