package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
                .build();
        caseData.setApplicant1Represented(NO);
        caseData.setResponseClaimTrack(MULTI_CLAIM.name());
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
                .build();
        caseData.setRespondent1Represented(NO);
        caseData.setSpecRespondent1Represented(NO);
        caseData.setResponseClaimTrack(MULTI_CLAIM.name());
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
                .build();
        caseData.setApplicant1Represented(NO);
        caseData.setResponseClaimTrack(INTERMEDIATE_CLAIM.name());
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
                .build();
        caseData.setSpecRespondent1Represented(NO);
        caseData.setRespondent1Represented(NO);
        caseData.setResponseClaimTrack(INTERMEDIATE_CLAIM.name());
        String actualState = service.setUpCaseState(caseData);

        assertEquals(CaseState.AWAITING_APPLICANT_INTENTION.name(), actualState);
    }

    @Test
    void shouldUpdateCaseStateToInMediation_WhenSmallClaimCarmEnabled1v1() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
        ClaimantMediationLip claimantMediationLip = new ClaimantMediationLip();
        claimantMediationLip.setHasAgreedFreeMediation(MediationDecision.No);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1ClaimMediationSpecRequiredLip(claimantMediationLip);
        CaseData caseData =
            CaseDataBuilder.builder().caseDataLip(caseDataLiP)
                .applicant1ProceedWithClaim(YES)
                .applicant1PartAdmitIntentionToSettleClaimSpec(NO)
                .atStateClaimIssued()
                .build();
        caseData.setResponseClaimTrack(SMALL_CLAIM.name());
        String actualState = service.setUpCaseState(caseData);

        assertEquals(CaseState.IN_MEDIATION.name(), actualState);
    }

    @Test
    void shouldUpdateCaseStateToJudicialReferral_WhenSmallClaimCarmNotEnabled() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        ClaimantMediationLip claimantMediationLip2 = new ClaimantMediationLip();
        claimantMediationLip2.setHasAgreedFreeMediation(MediationDecision.No);
        CaseDataLiP caseDataLiP2 = new CaseDataLiP();
        caseDataLiP2.setApplicant1ClaimMediationSpecRequiredLip(claimantMediationLip2);
        CaseData caseData =
            CaseDataBuilder.builder().caseDataLip(caseDataLiP2)
                .applicant1ProceedWithClaim(YES)
                .applicant1PartAdmitIntentionToSettleClaimSpec(NO)
                .atStateClaimIssued()
                .build();
        caseData.setResponseClaimTrack(SMALL_CLAIM.name());

        String actualState = service.setUpCaseState(caseData);

        assertEquals(CaseState.JUDICIAL_REFERRAL.name(), actualState);
    }

    @Test
    void shouldUpdateCaseStateToJudicialReferral_WhenPartAdmitNoSettle_NoMediation() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        //Given
        ClaimantMediationLip claimantMediationLip3 = new ClaimantMediationLip();
        claimantMediationLip3.setHasAgreedFreeMediation(MediationDecision.No);
        CaseDataLiP caseDataLiP3 = new CaseDataLiP();
        caseDataLiP3.setApplicant1ClaimMediationSpecRequiredLip(claimantMediationLip3);
        CaseData caseData = CaseDataBuilder.builder()
            .caseDataLip(caseDataLiP3)
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
        ClaimantMediationLip claimantMediationLip4 = new ClaimantMediationLip();
        claimantMediationLip4.setHasAgreedFreeMediation(MediationDecision.No);
        CaseDataLiP caseDataLiP4 = new CaseDataLiP();
        caseDataLiP4.setApplicant1ClaimMediationSpecRequiredLip(claimantMediationLip4);
        CaseData caseData = CaseDataBuilder.builder()
            .caseDataLip(caseDataLiP4)
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
        ClaimantMediationLip claimantMediationLip5 = new ClaimantMediationLip();
        claimantMediationLip5.setHasAgreedFreeMediation(MediationDecision.No);
        CaseDataLiP caseDataLiP5 = new CaseDataLiP();
        caseDataLiP5.setApplicant1ClaimMediationSpecRequiredLip(claimantMediationLip5);
        CaseData caseData =
            CaseDataBuilder.builder().caseDataLip(caseDataLiP5).applicant1PartAdmitIntentionToSettleClaimSpec(NO)
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
        ClaimantMediationLip claimantMediationLip6 = new ClaimantMediationLip();
        claimantMediationLip6.setHasAgreedFreeMediation(MediationDecision.No);
        CaseDataLiP caseDataLiP6 = new CaseDataLiP();
        caseDataLiP6.setApplicant1ClaimMediationSpecRequiredLip(claimantMediationLip6);
        CaseData caseData =
            CaseDataBuilder.builder().caseDataLip(caseDataLiP6).applicant1ProceedWithClaim(YES)
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
        ClaimantMediationLip claimantMediationLip7 = new ClaimantMediationLip();
        claimantMediationLip7.setHasAgreedFreeMediation(MediationDecision.Yes);
        CaseDataLiP caseDataLiP7 = new CaseDataLiP();
        caseDataLiP7.setApplicant1ClaimMediationSpecRequiredLip(claimantMediationLip7);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .applicant1PartAdmitConfirmAmountPaidSpec(NO)
            .caseDataLip(caseDataLiP7)
            .build();
        caseData.setResponseClaimMediationSpecRequired(YES);

        //When
        var response = service.setUpCaseState(caseData);
        //Then
        assertThat(response).isEqualTo(CaseState.IN_MEDIATION.name());
    }

    @Test
    void shouldNotChangeCaseState_whenHaveFullAdmissionFromRespondent() {
        //Given
        ClaimantMediationLip claimantMediationLip8 = new ClaimantMediationLip();
        claimantMediationLip8.setHasAgreedFreeMediation(MediationDecision.Yes);
        CaseDataLiP caseDataLiP8 = new CaseDataLiP();
        caseDataLiP8.setApplicant1ClaimMediationSpecRequiredLip(claimantMediationLip8);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .atStateRespondentRespondToClaimSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .applicant1PartAdmitConfirmAmountPaidSpec(NO)
            .caseDataLip(caseDataLiP8)
            .build();
        caseData.setResponseClaimMediationSpecRequired(YES);
        //When
        var response = service.setUpCaseState(caseData);
        //Then
        assertThat(response).isNotEqualTo(CaseState.IN_MEDIATION.name());
    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void shouldChangeCaseState_whenApplicantAgreeClaimSettlement(boolean carmEnabled) {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(carmEnabled);
        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .applicant1PartAdmitIntentionToSettleClaimSpec(YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YES)
            .build();
        caseData.setResponseClaimMediationSpecRequired(YES);

        //When
        var response = service.setUpCaseState(caseData);
        //Then
        assertThat(response).isEqualTo(CaseState.CASE_SETTLED.name());
    }

    @Test
    void shouldChangeCaseState_whenApplicantRejectRepaymentPlanAndIsCompany_toAllFinalOrdersIssued() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        //Given
        ClaimantMediationLip claimantMediationLip9 = new ClaimantMediationLip();
        claimantMediationLip9.setHasAgreedFreeMediation(MediationDecision.No);
        CaseDataLiP caseDataLiP9 = new CaseDataLiP();
        caseDataLiP9.setApplicant1ClaimMediationSpecRequiredLip(claimantMediationLip9);
        Party applicant1 = new Party();
        applicant1.setType(Party.Type.COMPANY);
        applicant1.setCompanyName("CLAIMANT_ORG_NAME");
        Party respondent1 = new Party();
        respondent1.setType(COMPANY);
        respondent1.setCompanyName("Test Inc");
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1AcceptPartAdmitPaymentPlanSpec(NO)
            .caseDataLip(caseDataLiP9)
            .build();
        caseData.setApplicant1(applicant1);
        caseData.setRespondent1(respondent1);

        //When
        var response = service.setUpCaseState(caseData);

        //Then
        assertThat(response).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
    }

    @Test
    void shouldChangeCaseState_whenApplicantAcceptedPartAdmitImmediatePayment() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        //Given
        Party applicant1b = new Party();
        applicant1b.setType(Party.Type.COMPANY);
        applicant1b.setCompanyName("CLAIMANT_ORG_NAME");
        Party respondent1b = new Party();
        respondent1b.setType(COMPANY);
        respondent1b.setCompanyName("Test Inc");
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdState(CaseState.AWAITING_APPLICANT_INTENTION);
        caseData.setApplicant1AcceptAdmitAmountPaidSpec(YES);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setApplicant1RepaymentOptionForDefendantSpec(PaymentType.IMMEDIATELY);
        caseData.setApplicant1(applicant1b);
        caseData.setRespondent1(respondent1b);

        //When
        var response = service.setUpCaseState(caseData);

        //Then
        assertThat(response).isEqualTo(CaseState.AWAITING_APPLICANT_INTENTION.name());
    }

    @Test
    void shouldChangeCaseState_whenApplicantRejectedRepaymentPlanAndRequestCCJ_CourtAcceptsClaimantDecision_ForPartAmit() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        //Given
        Party applicant1c = new Party();
        applicant1c.setType(Party.Type.INDIVIDUAL);
        applicant1c.setPartyName("CLAIMANT_NAME");
        ClaimantLiPResponse claimantLiPResponse = new ClaimantLiPResponse();
        claimantLiPResponse.setClaimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT);
        claimantLiPResponse.setApplicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ);
        CaseDataLiP caseDataLiPc = new CaseDataLiP();
        caseDataLiPc.setApplicant1LiPResponse(claimantLiPResponse);
        Party respondent1c = new Party();
        respondent1c.setType(Party.Type.INDIVIDUAL);
        respondent1c.setPartyName("CLAIMANT_NAME");
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1(applicant1c);
        caseData.setCaseDataLiP(caseDataLiPc);
        caseData.setRespondent1(respondent1c);

        //When
        var response = service.setUpCaseState(caseData);

        //Then
        assertThat(response).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
    }

    @Test
    void shouldChangeCaseState_whenApplicantRejectedRepaymentPlanAndRequestCCJ_RejectedManualDetermination_ForPartAmit_PayBySetDate() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        //Given
        Party applicant1d = new Party();
        applicant1d.setType(Party.Type.INDIVIDUAL);
        applicant1d.setPartyName("CLAIMANT_NAME");
        ClaimantLiPResponse claimantLiPResponse2 = new ClaimantLiPResponse();
        claimantLiPResponse2.setClaimantResponseOnCourtDecision(ClaimantResponseOnCourtDecisionType.JUDGE_REPAYMENT_DATE);
        CaseDataLiP caseDataLiPd = new CaseDataLiP();
        caseDataLiPd.setApplicant1LiPResponse(claimantLiPResponse2);
        Party respondent1d = new Party();
        respondent1d.setType(Party.Type.INDIVIDUAL);
        respondent1d.setPartyName("CLAIMANT_NAME");
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1(applicant1d);
        caseData.setCaseDataLiP(caseDataLiPd);
        caseData.setRespondent1(respondent1d);

        //When
        var response = service.setUpCaseState(caseData);

        //Then
        assertThat(response).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
    }

    @Test
    void shouldChangeCaseState_whenApplicantRejectedRepaymentPlanAndRequestCCJ_AcceptManualDetermination_ForPartAmit_ForPayByInstalments() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        //Given
        Party applicant1e = new Party();
        applicant1e.setType(Party.Type.INDIVIDUAL);
        applicant1e.setPartyName("CLAIMANT_NAME");
        ClaimantLiPResponse claimantLiPResponse3 = new ClaimantLiPResponse();
        claimantLiPResponse3.setClaimantResponseOnCourtDecision(ClaimantResponseOnCourtDecisionType.ACCEPT_REPAYMENT_PLAN);
        claimantLiPResponse3.setApplicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ);
        CaseDataLiP caseDataLiPe = new CaseDataLiP();
        caseDataLiPe.setApplicant1LiPResponse(claimantLiPResponse3);
        Party respondent1e = new Party();
        respondent1e.setType(Party.Type.INDIVIDUAL);
        respondent1e.setPartyName("CLAIMANT_NAME");
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1(applicant1e);
        caseData.setCaseDataLiP(caseDataLiPe);
        caseData.setRespondent1(respondent1e);

        //When
        var response = service.setUpCaseState(caseData);

        //Then
        assertThat(response).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
    }

    @Test
    void shouldChangeCaseState_whenApplicantRejectedRepaymentPlanAndRequestCCJ_AcceptManualDetermination_ForPartAmit_ForPayBySetDate() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        //Given
        Party applicant1f = new Party();
        applicant1f.setType(Party.Type.INDIVIDUAL);
        applicant1f.setPartyName("CLAIMANT_NAME");
        ClaimantLiPResponse claimantLiPResponse4 = new ClaimantLiPResponse();
        claimantLiPResponse4.setClaimantResponseOnCourtDecision(ClaimantResponseOnCourtDecisionType.ACCEPT_REPAYMENT_DATE);
        claimantLiPResponse4.setApplicant1ChoosesHowToProceed(ChooseHowToProceed.REQUEST_A_CCJ);
        CaseDataLiP caseDataLiPf = new CaseDataLiP();
        caseDataLiPf.setApplicant1LiPResponse(claimantLiPResponse4);
        Party respondent1f = new Party();
        respondent1f.setType(Party.Type.INDIVIDUAL);
        respondent1f.setPartyName("CLAIMANT_NAME");
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1(applicant1f);
        caseData.setCaseDataLiP(caseDataLiPf);
        caseData.setRespondent1(respondent1f);

        //When
        var response = service.setUpCaseState(caseData);

        //Then
        assertThat(response).isEqualTo(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
    }

    @Test
    void shouldUpdateCaseStateToAllFinalOrderIssued_whenApplicantAcceptOrRejectedRepaymentPlanAndRequestCCJ_JudgementOnlineLiveEnabled() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
        Party applicant1g = new Party();
        applicant1g.setType(Party.Type.INDIVIDUAL);
        applicant1g.setPartyName("CLAIMANT_NAME");
        Party respondent1g = new Party();
        respondent1g.setType(Party.Type.INDIVIDUAL);
        respondent1g.setPartyName("DEFENDANT_NAME");
        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails();
        ccjPaymentDetails.setCcjPaymentPaidSomeOption(YES);
        ccjPaymentDetails.setCcjJudgmentFixedCostAmount(BigDecimal.valueOf(10));
        ccjPaymentDetails.setCcjJudgmentTotalStillOwed(BigDecimal.valueOf(150));
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1ResponseDate(LocalDateTime.now())
            .respondent1Represented(NO)
            .specRespondent1Represented(NO)
            .applicant1Represented(NO)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YES)
            .build();
        caseData.setApplicant1(applicant1g);
        caseData.setRespondent1(respondent1g);
        caseData.setCcjPaymentDetails(ccjPaymentDetails);
        String actualState = service.setUpCaseState(caseData);

        assertEquals(CaseState.All_FINAL_ORDERS_ISSUED.name(), actualState);
    }
}
