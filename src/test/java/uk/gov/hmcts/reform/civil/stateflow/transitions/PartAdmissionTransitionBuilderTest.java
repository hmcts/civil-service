package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.citizenui.MediationLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.MediationLiPCarm;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.mediation.MediationContactInformation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.FullDefenceTransitionBuilder.isCarmApplicableCase;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.FullDefenceTransitionBuilder.isCarmApplicableLipCase;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.PartAdmissionTransitionBuilder.agreePartAdmitSettle;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.PartAdmissionTransitionBuilder.getCarmEnabledForCase;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.PartAdmissionTransitionBuilder.getCarmEnabledForLipCase;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.PartAdmissionTransitionBuilder.isClaimantNotSettlePartAdmitClaim;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.PartAdmissionTransitionBuilder.isSpecSmallClaim;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.PartAdmissionTransitionBuilder.partAdmitPayImmediately;

@ExtendWith(MockitoExtension.class)
class PartAdmissionTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        PartAdmissionTransitionBuilder partAdmissionTransitionBuilder = new PartAdmissionTransitionBuilder(
            mockFeatureToggleService);
        result = partAdmissionTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(11);

        assertTransition(result.get(0), "MAIN.PART_ADMISSION", "MAIN.IN_MEDIATION");
        assertTransition(result.get(1), "MAIN.PART_ADMISSION", "MAIN.IN_MEDIATION");
        assertTransition(result.get(2), "MAIN.PART_ADMISSION", "MAIN.PART_ADMIT_NOT_SETTLED_NO_MEDIATION");
        assertTransition(result.get(3), "MAIN.PART_ADMISSION", "MAIN.PART_ADMIT_PROCEED");
        assertTransition(result.get(4), "MAIN.PART_ADMISSION", "MAIN.PART_ADMIT_NOT_PROCEED");
        assertTransition(result.get(5), "MAIN.PART_ADMISSION", "MAIN.PART_ADMIT_PAY_IMMEDIATELY");
        assertTransition(result.get(6), "MAIN.PART_ADMISSION", "MAIN.PART_ADMIT_AGREE_SETTLE");
        assertTransition(result.get(7), "MAIN.PART_ADMISSION", "MAIN.PART_ADMIT_AGREE_REPAYMENT");
        assertTransition(result.get(8), "MAIN.PART_ADMISSION", "MAIN.PART_ADMIT_REJECT_REPAYMENT");
        assertTransition(result.get(9), "MAIN.PART_ADMISSION", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(10), "MAIN.PART_ADMISSION", "MAIN.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA");
    }

    @Test
    void shouldReturnTrue_whenPartAdmitClaimIsSettled() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .build();

        assertTrue(agreePartAdmitSettle.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenIsPartAdmitClaimSpecIsFalse() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .build();

        assertFalse(agreePartAdmitSettle.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenIsClaimantIntentionSettlePartAdmitIsFalse() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .build();

        assertFalse(agreePartAdmitSettle.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenIsClaimantConfirmAmountPaidPartAdmitIsFalse() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.NO)
            .build();

        assertFalse(agreePartAdmitSettle.test(caseData));
    }

    @Test
    void isPartAdmitPayImmediatelyAccepted_thenTrue() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();

        assertTrue(partAdmitPayImmediately.test(caseData));
    }

    @Test
    void isClaimantNotSettlePartAdmitClaimDefendantNotPaidClaimantRejects_thenTrue() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();

        assertTrue(isClaimantNotSettlePartAdmitClaim.test(caseData));
    }

    @Test
    void isClaimantNotSettlePartAdmitClaimStatesPaidRejectsPA_thenTrue() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();

        assertTrue(isClaimantNotSettlePartAdmitClaim.test(caseData));
    }

    @Test
    void isClaimantNotSettlePartAdmitClaimStatesPaidButNotPaid_thenTrue() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.NO)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();

        assertTrue(isClaimantNotSettlePartAdmitClaim.test(caseData));
    }

    @Test
    void isPartAdmitPayImmediatelyAccepted_thenFalse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(partAdmitPayImmediately.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseAccessCategoryIsNotSpecClaim() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();

        assertFalse(partAdmitPayImmediately.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenApplicant1AcceptAdmitAmountPaidSpecIsNotYes() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();

        assertFalse(partAdmitPayImmediately.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenShowResponseOneVOneFlagIsNull() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .showResponseOneVOneFlag(null)
            .build();

        assertFalse(partAdmitPayImmediately.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenShowResponseOneVOneFlagIsNotOneVOnePartAdmitPayImmediately() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_FULL_DEFENCE)
            .build();

        assertFalse(partAdmitPayImmediately.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCarmApplicableForDefendantLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssuedUnrepresentedDefendant()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1MediationLiPResponseCarm(MediationLiPCarm.builder()
                                                                      .isMediationContactNameCorrect(YES)
                                                                      .build())
                             .build())
            .build();

        assertTrue(isCarmApplicableLipCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmNotApplicableForDefendantLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssuedUnrepresentedDefendant()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                         .respondent1MediationLiPResponse(MediationLiP.builder()
                                                                                              .canWeUseMediationLiP(YES)
                                                                                              .build())
                                                         .build()).build()).build();

        assertFalse(isCarmApplicableLipCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmNotApplicableForDefendantLipFastTrack() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssuedUnrepresentedDefendant()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(FAST_CLAIM.name())
            .caseDataLiP(CaseDataLiP.builder()
                             .respondent1MediationLiPResponseCarm(MediationLiPCarm.builder()
                                                                      .isMediationContactNameCorrect(YES)
                                                                      .build())
                             .build())
            .build();

        assertFalse(isCarmApplicableLipCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_when1v2ClaimantLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(NO)
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .respondent2(Party.builder().build())
            .respondent1Represented(YES)
            .responseClaimTrack(SMALL_CLAIM.name())
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponseCarm(MediationLiPCarm.builder()
                                                            .isMediationContactNameCorrect(YES)
                                                            .build())
                             .build())
            .build();

        assertFalse(isCarmApplicableLipCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_when1v2RespondentLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(YES)
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .respondent2(Party.builder().build())
            .respondent1Represented(NO)
            .responseClaimTrack(SMALL_CLAIM.name())
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponseCarm(MediationLiPCarm.builder()
                                                            .isMediationContactNameCorrect(YES)
                                                            .build())
                             .build())
            .build();

        assertFalse(isCarmApplicableLipCase.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCarmApplicableForClaimantLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(NO)
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponseCarm(MediationLiPCarm.builder()
                                                            .isMediationContactNameCorrect(YES)
                                                            .build())
                             .build())
            .build();

        assertTrue(isCarmApplicableLipCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmNotApplicableForClaimantLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(NO)
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder().build())
                             .build())
            .build();

        assertFalse(isCarmApplicableLipCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmApplicableForClaimantLipUnspecCase() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(NO)
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1LiPResponseCarm(MediationLiPCarm.builder()
                                                            .isMediationContactNameCorrect(YES)
                                                            .build())
                             .build())
            .build();

        assertFalse(isCarmApplicableLipCase.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCarmApplicableForClaimantFullDefenceProceed() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .app1MediationContactInfo(MediationContactInformation.builder()
                                          .firstName("name")
                                          .build())
            .build();

        assertTrue(isCarmApplicableCase.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCarmApplicableForDefendantFullDefenceProceed() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .resp1MediationContactInfo(MediationContactInformation.builder()
                                           .firstName("name")
                                           .build())
            .build();

        assertTrue(isCarmApplicableCase.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCarmApplicableForDefendant2FullDefenceProceed() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .resp2MediationContactInfo(MediationContactInformation.builder()
                                           .firstName("name")
                                           .build())
            .build();

        assertTrue(isCarmApplicableCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenRespondentLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .respondent1Represented(NO)
            .resp1MediationContactInfo(MediationContactInformation.builder()
                                           .firstName("name")
                                           .build())
            .build();

        assertFalse(isCarmApplicableCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenApplicantLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .applicant1Represented(NO)
            .resp1MediationContactInfo(MediationContactInformation.builder()
                                           .firstName("name")
                                           .build())
            .build();

        assertFalse(isCarmApplicableCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmApplicableFastClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .atState2v1Applicant1NotProceedApplicant2Proceeds()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(FAST_CLAIM.name())
            .resp1MediationContactInfo(MediationContactInformation.builder()
                                           .firstName("name")
                                           .build())
            .build();

        assertFalse(isCarmApplicableCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmApplicableUnspecClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .atState2v1Applicant1NotProceedApplicant2Proceeds()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .resp1MediationContactInfo(MediationContactInformation.builder()
                                           .firstName("name")
                                           .build())
            .build();

        assertFalse(isCarmApplicableCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_UnspecFastClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .atState2v1Applicant1NotProceedApplicant2Proceeds()
            .build().toBuilder()
            .responseClaimTrack(FAST_CLAIM.name())
            .build();

        assertFalse(isSpecSmallClaim(caseData));
    }

    @Test
    void shouldReturnTrue_SpecSmallClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .atState2v1Applicant1NotProceedApplicant2Proceeds()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .build();

        assertTrue(isSpecSmallClaim(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmNotEnabledOnLipClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .atState2v1Applicant1NotProceedApplicant2Proceeds()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1SettleClaim(YES)
                             .build())
            .build();

        assertFalse(getCarmEnabledForLipCase(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmNotEnabledOnLRClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .atState2v1Applicant1NotProceedApplicant2Proceeds()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .build();

        assertFalse(getCarmEnabledForCase(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
