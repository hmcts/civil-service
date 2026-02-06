package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ClaimPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.ClaimantPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.MediationPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.PaymentPredicate;
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

import static uk.gov.hmcts.reform.civil.stateflow.transitions.PartAdmissionTransitionBuilder.isNotPartAdmissionPaymentState;

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

        assertTrue(ClaimPredicate.isPartAdmitSettled.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenIsPartAdmitClaimSpecIsFalse() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .build();

        assertFalse(ClaimPredicate.isPartAdmitSettled.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenIsClaimantIntentionSettlePartAdmitIsFalse() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.YES)
            .build();

        assertFalse(ClaimPredicate.isPartAdmitSettled.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenIsClaimantConfirmAmountPaidPartAdmitIsFalse() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.YES)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.NO)
            .build();

        assertFalse(ClaimPredicate.isPartAdmitSettled.test(caseData));
    }

    @Test
    void isPartAdmitPayImmediatelyAccepted_thenTrue() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();

        assertTrue(PaymentPredicate.payImmediatelyAcceptedPartAdmit.test(caseData));
    }

    @Test
    void isClaimantNotSettlePartAdmitClaimDefendantNotPaidClaimantRejects_thenTrue() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();

        assertTrue(ClaimantPredicate.isNotSettlePartAdmit.test(caseData));
    }

    @Test
    void isClaimantNotSettlePartAdmitClaimStatesPaidRejectsPA_thenTrue() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo.NO)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();

        assertTrue(ClaimantPredicate.isNotSettlePartAdmit.test(caseData));
    }

    @Test
    void isClaimantNotSettlePartAdmitClaimStatesPaidButNotPaid_thenTrue() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo.NO)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();

        assertTrue(ClaimantPredicate.isNotSettlePartAdmit.test(caseData));
    }

    @Test
    void isPartAdmitPayImmediatelyAccepted_thenFalse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(PaymentPredicate.payImmediatelyAcceptedPartAdmit.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseAccessCategoryIsNotSpecClaim() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();

        assertFalse(PaymentPredicate.payImmediatelyAcceptedPartAdmit.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenApplicant1AcceptAdmitAmountPaidSpecIsNotYes() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.NO)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();

        assertFalse(PaymentPredicate.payImmediatelyAcceptedPartAdmit.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenShowResponseOneVOneFlagIsNull() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .showResponseOneVOneFlag(null)
            .build();

        assertFalse(PaymentPredicate.payImmediatelyAcceptedPartAdmit.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenShowResponseOneVOneFlagIsNotOneVOnePartAdmitPayImmediately() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YesOrNo.YES)
            .showResponseOneVOneFlag(ResponseOneVOneShowTag.ONE_V_ONE_FULL_DEFENCE)
            .build();

        assertFalse(PaymentPredicate.payImmediatelyAcceptedPartAdmit.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCarmApplicableForDefendantLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssuedUnrepresentedDefendant()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .caseDataLiP(new CaseDataLiP()
                             .setRespondent1MediationLiPResponseCarm(new MediationLiPCarm()
                                                                      .setIsMediationContactNameCorrect(YES)))
            .build();

        assertTrue(MediationPredicate.isCarmApplicableCaseLiP.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmNotApplicableForDefendantLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssuedUnrepresentedDefendant()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .caseDataLiP(new CaseDataLiP()
                             .setRespondent1LiPResponse(new RespondentLiPResponse()
                                                         .setRespondent1MediationLiPResponse(new MediationLiP()
                                                                                              .setCanWeUseMediationLiP(YES)))).build();

        assertFalse(MediationPredicate.isCarmApplicableCaseLiP.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmNotApplicableForDefendantLipFastTrack() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssuedUnrepresentedDefendant()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(FAST_CLAIM.name())
            .caseDataLiP(new CaseDataLiP()
                             .setRespondent1MediationLiPResponseCarm(new MediationLiPCarm()
                                                                      .setIsMediationContactNameCorrect(YES)))
            .build();

        assertFalse(MediationPredicate.isCarmApplicableCaseLiP.test(caseData));
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
            .caseDataLiP(new CaseDataLiP()
                             .setApplicant1LiPResponseCarm(new MediationLiPCarm()
                                                            .setIsMediationContactNameCorrect(YES)))
            .build();

        assertFalse(MediationPredicate.isCarmApplicableCaseLiP.test(caseData));
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
            .caseDataLiP(new CaseDataLiP()
                             .setApplicant1LiPResponseCarm(new MediationLiPCarm()
                                                            .setIsMediationContactNameCorrect(YES)))
            .build();

        assertFalse(MediationPredicate.isCarmApplicableCaseLiP.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCarmApplicableForClaimantLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(NO)
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .caseDataLiP(new CaseDataLiP()
                             .setApplicant1LiPResponseCarm(new MediationLiPCarm()
                                                            .setIsMediationContactNameCorrect(YES)))
            .build();

        assertTrue(MediationPredicate.isCarmApplicableCaseLiP.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmNotApplicableForClaimantLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(NO)
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .caseDataLiP(new CaseDataLiP()
                             .setApplicant1ClaimMediationSpecRequiredLip(new ClaimantMediationLip()))
            .build();

        assertFalse(MediationPredicate.isCarmApplicableCaseLiP.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmApplicableForClaimantLipUnspecCase() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(NO)
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .caseDataLiP(new CaseDataLiP()
                             .setApplicant1LiPResponseCarm(new MediationLiPCarm()
                                                            .setIsMediationContactNameCorrect(YES)))
            .build();

        assertFalse(MediationPredicate.isCarmApplicableCaseLiP.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCarmApplicableForClaimantPartAdmitProceed() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .app1MediationContactInfo(new MediationContactInformation().setFirstName("name"))
            .build();

        assertTrue(MediationPredicate.isCarmApplicableCase.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCarmApplicableForDefendantPartAdmitProceed() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .resp1MediationContactInfo(new MediationContactInformation().setFirstName("name"))
            .build();

        assertTrue(MediationPredicate.isCarmApplicableCase.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCarmApplicableForDefendant2PartAdmitProceed() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .resp2MediationContactInfo(new MediationContactInformation().setFirstName("name"))
            .build();

        assertTrue(MediationPredicate.isCarmApplicableCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenRespondentLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .respondent1Represented(NO)
            .resp1MediationContactInfo(new MediationContactInformation().setFirstName("name"))
            .build();

        assertFalse(MediationPredicate.isCarmApplicableCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenApplicantLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .applicant1Represented(NO)
            .resp1MediationContactInfo(new MediationContactInformation().setFirstName("name"))
            .build();

        assertFalse(MediationPredicate.isCarmApplicableCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmApplicableFastClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .atState2v1Applicant1NotProceedApplicant2Proceeds()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(FAST_CLAIM.name())
            .resp1MediationContactInfo(new MediationContactInformation().setFirstName("name"))
            .build();

        assertFalse(MediationPredicate.isCarmApplicableCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmApplicableUnspecClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .atState2v1Applicant1NotProceedApplicant2Proceeds()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .resp1MediationContactInfo(new MediationContactInformation().setFirstName("name"))
            .build();

        assertFalse(MediationPredicate.isCarmApplicableCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmNotEnabledOnLipClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .atState2v1Applicant1NotProceedApplicant2Proceeds()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .caseDataLiP(new CaseDataLiP()
                             .setApplicant1SettleClaim(YES))
            .build();

        assertFalse(MediationPredicate.isCarmEnabledForCaseLiP.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCarmEnabledOnLipClaimApplicant() {
        CaseData caseData = CaseDataBuilder.builder()
            .atState2v1Applicant1NotProceedApplicant2Proceeds()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .caseDataLiP(new CaseDataLiP()
                             .setApplicant1LiPResponseCarm(new MediationLiPCarm()
                                                            .setIsMediationContactNameCorrect(YES)))
            .build();

        assertTrue(MediationPredicate.isCarmEnabledForCaseLiP.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCarmEnabledOnLipClaimDefendant() {
        CaseData caseData = CaseDataBuilder.builder()
            .atState2v1Applicant1NotProceedApplicant2Proceeds()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .caseDataLiP(new CaseDataLiP()
                             .setRespondent1MediationLiPResponseCarm(new MediationLiPCarm()
                                                                      .setIsMediationContactNameCorrect(YES)))
            .build();

        assertTrue(MediationPredicate.isCarmEnabledForCaseLiP.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmNotEnabledOnLRClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .atState2v1Applicant1NotProceedApplicant2Proceeds()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .build();

        assertFalse(MediationPredicate.isCarmEnabledForCase.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCarmEnabledOnLRClaimApplicant() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .build().toBuilder()
            .app1MediationContactInfo(new MediationContactInformation().setFirstName("name"))
            .build();

        assertTrue(MediationPredicate.isCarmEnabledForCase.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCarmEnabledOnLRClaimDefendant1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .build().toBuilder()
            .resp1MediationContactInfo(new MediationContactInformation().setFirstName("name"))
            .build();

        assertTrue(MediationPredicate.isCarmEnabledForCase.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCarmEnabledOnLRClaimDefendant2() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .build().toBuilder()
            .resp2MediationContactInfo(new MediationContactInformation().setFirstName("name"))
            .build();

        assertTrue(MediationPredicate.isCarmEnabledForCase.test(caseData));
    }

    @ParameterizedTest
    @EnumSource(value = ResponseOneVOneShowTag.class, names = {
        "ONE_V_ONE_PART_ADMIT_PAY_INSTALMENT",
        "ONE_V_ONE_PART_ADMIT_PAY_BY_SET_DATE",
        "ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY",
        "ONE_V_ONE_PART_ADMIT_HAS_PAID"
    })
    void shouldReturnFalseForOneVOnePartAdmit(ResponseOneVOneShowTag tag) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued().build().toBuilder()
            .showResponseOneVOneFlag(tag)
            .build();

        assertFalse(isNotPartAdmissionPaymentState.test(caseData));
    }

    @Test
    void shouldReturnTrueIfNotOneVOnePartAdmit() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued().build().toBuilder()
            .build();

        assertTrue(isNotPartAdmissionPaymentState.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
