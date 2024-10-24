package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.FullDefenceTransitionBuilder.allAgreedToLrMediationSpec;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.FullDefenceTransitionBuilder.declinedMediation;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.FullDefenceTransitionBuilder.demageMultiClaim;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.FullDefenceTransitionBuilder.getPredicateTakenOfflineByStaffAfterDefendantResponseBeforeClaimantResponse;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.FullDefenceTransitionBuilder.isCarmApplicableLipCase;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.FullDefenceTransitionBuilder.isClaimantNotSettleFullDefenceClaim;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.FullDefenceTransitionBuilder.isDefendantNotPaidFullDefenceClaim;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.FullDefenceTransitionBuilder.lipFullDefenceProceed;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.FullDefenceTransitionBuilder.takenOfflineByStaffAfterDefendantResponse;

@ExtendWith(MockitoExtension.class)
public class FullDefenceTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        FullDefenceTransitionBuilder fullDefenceTransitionBuilder = new FullDefenceTransitionBuilder(mockFeatureToggleService);
        result = fullDefenceTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(9);

        assertTransition(result.get(0), "MAIN.FULL_DEFENCE", "MAIN.IN_MEDIATION");
        assertTransition(result.get(1), "MAIN.FULL_DEFENCE", "MAIN.FULL_DEFENCE_PROCEED");
        assertTransition(result.get(2), "MAIN.FULL_DEFENCE", "MAIN.FULL_DEFENCE_PROCEED");
        assertTransition(result.get(3), "MAIN.FULL_DEFENCE", "MAIN.FULL_DEFENCE_PROCEED");
        assertTransition(result.get(4), "MAIN.FULL_DEFENCE", "MAIN.FULL_DEFENCE_PROCEED");
        assertTransition(result.get(5), "MAIN.FULL_DEFENCE", "MAIN.FULL_DEFENCE_PROCEED");
        assertTransition(result.get(6), "MAIN.FULL_DEFENCE", "MAIN.FULL_DEFENCE_NOT_PROCEED");
        assertTransition(result.get(7), "MAIN.FULL_DEFENCE", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(8), "MAIN.FULL_DEFENCE", "MAIN.PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA");
    }

    @Test
    void shouldReturnFalse_whenNotLipFullDefenceProceed() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1SettleClaim(YES)
                             .build())
            .build();

        assertFalse(lipFullDefenceProceed.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCarmApplicableForDefendantLip() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssuedUnrepresentedDefendant()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .submittedDate(LocalDateTime.of(2028, 6, 1, 1, 1))
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
            .submittedDate(LocalDateTime.of(2000, 6, 1, 1, 1))
            .build();

        assertFalse(isCarmApplicableLipCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmNotApplicableForDefendantLipFastTrack() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssuedUnrepresentedDefendant()
            .setClaimTypeToSpecClaim()
            .build().toBuilder()
            .responseClaimTrack(FAST_CLAIM.name())
            .submittedDate(LocalDateTime.of(2028, 6, 1, 1, 1))
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
            .submittedDate(LocalDateTime.of(2028, 6, 1, 1, 1))
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
            .submittedDate(LocalDateTime.of(2000, 6, 1, 1, 1))
            .build();

        assertFalse(isCarmApplicableLipCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCarmApplicableForClaimantLipUnspecCase() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1Represented(NO)
            .build().toBuilder()
            .responseClaimTrack(SMALL_CLAIM.name())
            .submittedDate(LocalDateTime.of(2028, 6, 1, 1, 1))
            .build();

        assertFalse(isCarmApplicableLipCase.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineBeforeClaimantResponseAfterDefendantResponse_1v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .takenOfflineByStaff().build();
        assertTrue(takenOfflineByStaffAfterDefendantResponse.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineBeforeClaimantResponseAfterDefendantResponse_1v2DS() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateRespondentFullDefence()
            .respondent2Responds(FULL_DEFENCE)
            .takenOfflineByStaff().build();
        assertTrue(takenOfflineByStaffAfterDefendantResponse.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineBeforeClaimantResponseAfterDefendantResponse_1v2SS() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateBothRespondentsSameResponse(FULL_DEFENCE)
            .takenOfflineByStaff().build();
        assertTrue(takenOfflineByStaffAfterDefendantResponse.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenTakenOfflineBeforeClaimantResponseAfterDefendantResponse_2v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atStateRespondentFullDefence()
            .takenOfflineByStaff().build();
        assertTrue(takenOfflineByStaffAfterDefendantResponse.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineAfterClaimantResponse() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .takenOfflineByStaff().build();
        assertFalse(takenOfflineByStaffAfterDefendantResponse.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenMultiClaimAndUnspecClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmittedMultiClaim()
            .caseAccessCategory(UNSPEC_CLAIM)
            .build();

        assertTrue(demageMultiClaim.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenNotMultiClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmittedSmallClaim()
            .caseAccessCategory(UNSPEC_CLAIM)
            .build();

        assertFalse(demageMultiClaim.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenNotUnspecClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmittedMultiClaim()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .build();

        assertFalse(demageMultiClaim.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenNeitherMultiClaimNorUnspecClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmittedSmallClaim()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .build();

        assertFalse(demageMultiClaim.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenUnspec_false() {
        CaseData caseData = CaseData.builder().build();
        assertFalse(allAgreedToLrMediationSpec.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenNotSmall_false() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .build();
        assertFalse(allAgreedToLrMediationSpec.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenAllAgreedToLrMediationSpec_1v1() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .responseClaimTrack(SMALL_CLAIM.name())
            .build();

        Map<YesOrNo[], Boolean> defClaim = Map.of(
            new YesOrNo[]{null, null, NO}, false,
            new YesOrNo[]{NO, NO, NO}, false,
            new YesOrNo[]{NO, YES, NO}, false,
            new YesOrNo[]{YES, NO, NO}, false,
            new YesOrNo[]{YES, NO, YES}, false,
            new YesOrNo[]{YES, YES, YES}, true
        );

        defClaim.forEach((whoAgrees, expected) -> {
            CaseData cd = caseData.toBuilder()
                .responseClaimMediationSpecRequired(whoAgrees[0])
                .applicant1ClaimMediationSpecRequired(SmallClaimMedicalLRspec.builder()
                                                          .hasAgreedFreeMediation(whoAgrees[1])
                                                          .build())
                .build();
            assertEquals(expected, allAgreedToLrMediationSpec.test(cd));
        });
    }

    @Test
    void shouldReturnTrue_whenAllAgreedToLrMediationSpec_1v2SS() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .responseClaimTrack(SMALL_CLAIM.name())
            .respondent2(Party.builder().build())
            .respondent2SameLegalRepresentative(YES)
            .build();

        Map<YesOrNo[], Boolean> defClaim = Map.of(
            new YesOrNo[]{null, null, NO}, false,
            new YesOrNo[]{NO, NO, NO}, false,
            new YesOrNo[]{NO, YES, NO}, false,
            new YesOrNo[]{YES, NO, NO}, false,
            new YesOrNo[]{YES, NO, YES}, false,
            new YesOrNo[]{YES, YES, YES}, true
        );

        defClaim.forEach((whoAgrees, expected) -> {
            CaseData cd = caseData.toBuilder()
                .responseClaimMediationSpecRequired(whoAgrees[0])
                .applicant1ClaimMediationSpecRequired(SmallClaimMedicalLRspec.builder()
                                                          .hasAgreedFreeMediation(whoAgrees[1])
                                                          .build())
                .build();
            assertEquals(expected, allAgreedToLrMediationSpec.test(cd));
        });
    }

    @Test
    void shouldReturnTrue_whenAllAgreedToLrMediationSpec_1v2DS() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .responseClaimTrack(SMALL_CLAIM.name())
            .respondent2(Party.builder().build())
            .respondent2SameLegalRepresentative(NO)
            .build();

        Map<YesOrNo[], Boolean> defClaim = Map.of(
            new YesOrNo[]{null, null, null, NO}, false,
            new YesOrNo[]{NO, NO, NO, NO}, false,
            new YesOrNo[]{NO, NO, YES, NO}, false,
            new YesOrNo[]{NO, YES, NO, NO}, false,
            new YesOrNo[]{NO, YES, YES, NO}, false,
            new YesOrNo[]{YES, NO, NO, NO}, false,
            new YesOrNo[]{YES, NO, YES, NO}, false,
            new YesOrNo[]{YES, YES, NO, NO}, false,
            new YesOrNo[]{YES, NO, NO, YES}, false,
            new YesOrNo[]{YES, YES, YES, YES}, true
        );

        defClaim.forEach((whoAgrees, expected) -> {
            CaseData cd = caseData.toBuilder()
                .responseClaimMediationSpecRequired(whoAgrees[0])
                .responseClaimMediationSpec2Required(whoAgrees[1])
                .applicant1ClaimMediationSpecRequired(SmallClaimMedicalLRspec.builder()
                                                          .hasAgreedFreeMediation(whoAgrees[2])
                                                          .build())
                .build();
            assertEquals(expected, allAgreedToLrMediationSpec.test(cd));
        });
    }

    @Test
    void shouldReturnTrue_whenAllAgreedToLrMediationSpec_2v1() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .responseClaimTrack(SMALL_CLAIM.name())
            .build();

        Map<YesOrNo[], Boolean> defClaim = Map.of(
            new YesOrNo[]{null, null, null, NO}, false,
            new YesOrNo[]{NO, NO, NO, NO}, false,
            new YesOrNo[]{NO, NO, YES, NO}, false,
            new YesOrNo[]{NO, YES, NO, NO}, false,
            new YesOrNo[]{NO, YES, YES, NO}, false,
            new YesOrNo[]{YES, NO, NO, NO}, false,
            new YesOrNo[]{YES, NO, YES, NO}, false,
            new YesOrNo[]{YES, YES, NO, NO}, false,
            new YesOrNo[]{YES, NO, NO, YES}, false,
            new YesOrNo[]{YES, YES, YES, YES}, true
        );

        defClaim.forEach((whoAgrees, expected) -> {
            CaseData cd = caseData.toBuilder()
                .responseClaimMediationSpecRequired(whoAgrees[0])
                .applicant1ClaimMediationSpecRequired(SmallClaimMedicalLRspec.builder()
                                                          .hasAgreedFreeMediation(whoAgrees[1])
                                                          .build())
                .applicantMPClaimMediationSpecRequired(SmallClaimMedicalLRspec.builder()
                                                           .hasAgreedFreeMediation(whoAgrees[2])
                                                           .build())
                .build();
            assertEquals(expected, allAgreedToLrMediationSpec.test(cd));
        });
    }

    @Test
    void shouldReturnFalse_whenClaimantAgreedToLipMediation() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .responseClaimTrack(SMALL_CLAIM.name())
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1ClaimMediationSpecRequiredLip(ClaimantMediationLip.builder()
                                                                          .hasAgreedFreeMediation(MediationDecision.Yes)
                                                                          .build())
                             .build())
            .build();
        boolean result = allAgreedToLrMediationSpec.test(caseData);
        assertFalse(result);
    }

    @Test
    void shouldReturnFalse_whenMediationUnsuccessful_OneVOne() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationUnsuccessful(MultiPartyScenario.ONE_V_ONE)
            .build();

        assertFalse(declinedMediation.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenMediationUnsuccessfulCarm_OneVOne() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationUnsuccessfulCarm(MultiPartyScenario.ONE_V_ONE)
            .build();

        assertFalse(declinedMediation.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenMediationSuccessful_OneVOne() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationSuccessful(MultiPartyScenario.ONE_V_ONE)
            .build();

        assertFalse(declinedMediation.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenMediationUnsuccessful_OneVTwoOneLegalRep() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationUnsuccessful(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
            .build();

        assertFalse(declinedMediation.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenMediationUnsuccessfulCarm_OneVTwoOneLegalRep() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationUnsuccessfulCarm(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
            .build();

        assertFalse(declinedMediation.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenMediationSuccessful_OneVTwoOneLegalRep() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationSuccessful(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
            .build();

        assertFalse(declinedMediation.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenMediationUnsuccessful_TwoVOne() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationUnsuccessful(MultiPartyScenario.TWO_V_ONE)
            .build();

        assertFalse(declinedMediation.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenMediationUnsuccessfulCarm_TwoVOne() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationUnsuccessfulCarm(MultiPartyScenario.TWO_V_ONE)
            .build();

        assertFalse(declinedMediation.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenMediationSuccessful_TwoVOne() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationSuccessful(MultiPartyScenario.TWO_V_ONE)
            .build();

        assertFalse(declinedMediation.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenMediationUnsuccessful_OneVTwoTwoLegalRep() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationUnsuccessful(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)
            .build();

        assertFalse(declinedMediation.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenMediationUnsuccessfulCarm_OneVTwoTwoLegalRep() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationUnsuccessfulCarm(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)
            .build();

        assertFalse(declinedMediation.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenMediationSuccessful_OneVTwoTwoLegalRep() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationSuccessful(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)
            .build();

        assertFalse(declinedMediation.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenClaimantDoesNotSettle() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1PartAdmitIntentionToSettleClaimSpec(NO)
            .build();

        assertTrue(isClaimantNotSettleFullDefenceClaim.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenClaimantSettles() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1PartAdmitIntentionToSettleClaimSpec(YES)
            .build();

        assertFalse(isClaimantNotSettleFullDefenceClaim.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenDefendantHasNotPaid() {
        CaseData caseData = CaseDataBuilder.builder()
            .build().toBuilder()
            .applicant1FullDefenceConfirmAmountPaidSpec(NO)
            .build();

        assertTrue(isDefendantNotPaidFullDefenceClaim.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenDefendantHasPaid() {
        CaseData caseData = CaseDataBuilder.builder()
            .build().toBuilder()
            .applicant1FullDefenceConfirmAmountPaidSpec(YES)
            .build();

        assertFalse(isDefendantNotPaidFullDefenceClaim.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenLipClaimantDoesNotSettle() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateLipClaimantDoesNotSettle()
            .setClaimTypeToSpecClaim()
            .build();
        assertTrue(lipFullDefenceProceed.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenSpecClaimAndApplicant1SettleClaimIsNo() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1SettleClaim(NO)
                             .build())
            .build();

        assertTrue(FullDefenceTransitionBuilder.getPredicateForLipClaimantIntentionProceed(caseData));
    }

    @Test
    void shouldReturnFalse_whenNotSpecClaimOrApplicant1SettleClaimIsNotNo() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1SettleClaim(YES)
                             .build())
            .build();

        assertFalse(FullDefenceTransitionBuilder.getPredicateForLipClaimantIntentionProceed(caseData));

        caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1SettleClaim(NO)
                             .build())
            .build();

        assertFalse(FullDefenceTransitionBuilder.getPredicateForLipClaimantIntentionProceed(caseData));
    }

    @Test
    void shouldReturnTrue_whenIsCarmApplicableLipCase() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .responseClaimTrack(SMALL_CLAIM.name())
            .respondent2(null)
            .applicant1Represented(NO)
            .respondent1Represented(NO)
            .submittedDate(LocalDateTime.of(2024, 11, 5, 0, 0))
            .build();

        assertTrue(isCarmApplicableLipCase.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenIsCarmApplicableLipCase() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .responseClaimTrack(SMALL_CLAIM.name())
            .respondent2(null)
            .applicant1Represented(NO)
            .respondent1Represented(NO)
            .submittedDate(LocalDateTime.of(2024, 8, 1, 0, 0))
            .build();

        assertFalse(isCarmApplicableLipCase.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenIsSpecSmallClaim() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .responseClaimTrack(SMALL_CLAIM.name())
            .build();

        assertTrue(FullDefenceTransitionBuilder.isSpecSmallClaim(caseData));
    }

    @Test
    void shouldReturnFalse_whenIsSpecSmallClaim() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .responseClaimTrack(SMALL_CLAIM.name())
            .build();

        assertFalse(FullDefenceTransitionBuilder.isSpecSmallClaim(caseData));
    }

    @Test
    void shouldReturnTrue_whenGetCarmEnabledForDate() {
        CaseData caseData = CaseData.builder()
            .submittedDate(LocalDateTime.of(2024, 11, 5, 0, 0))
            .build();

        assertTrue(FullDefenceTransitionBuilder.getCarmEnabledForDate(caseData));
    }

    @Test
    void shouldReturnFalse_whenGetCarmEnabledForDate() {
        CaseData caseData = CaseData.builder()
            .submittedDate(LocalDateTime.of(2024, 7, 31, 0, 0))
            .build();

        assertFalse(FullDefenceTransitionBuilder.getCarmEnabledForDate(caseData));
    }

    @Test
    void shouldReturnFalse_whenTakenOfflineByStaffDateIsNull() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertFalse(getPredicateTakenOfflineByStaffAfterDefendantResponseBeforeClaimantResponse(caseData));
    }

    @Test
    void shouldReturnFalse_whenApplicant1ResponseDateIsNotNull() {
        CaseData caseData = CaseDataBuilder.builder().applicant1ResponseDate(LocalDateTime.now()).build();
        assertFalse(getPredicateTakenOfflineByStaffAfterDefendantResponseBeforeClaimantResponse(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseAccessCategoryIsNotUnspecClaim() {
        CaseData caseData = CaseDataBuilder.builder().caseAccessCategory(CaseCategory.SPEC_CLAIM).build();
        assertFalse(getPredicateTakenOfflineByStaffAfterDefendantResponseBeforeClaimantResponse(caseData));
    }

    @Test
    void shouldReturnFalse_whenAddApplicant2IsNotYes() {
        CaseData caseData = CaseDataBuilder.builder().addApplicant2(NO).build();
        assertFalse(getPredicateTakenOfflineByStaffAfterDefendantResponseBeforeClaimantResponse(caseData));
    }

    @Test
    void shouldReturnFalse_whenApplicant2ResponseDateIsNotNull() {
        CaseData caseData = CaseDataBuilder.builder().applicant2ResponseDate(LocalDateTime.now()).build();
        assertFalse(getPredicateTakenOfflineByStaffAfterDefendantResponseBeforeClaimantResponse(caseData));
    }

    @Test
    void shouldReturnTrue_whenAllConditionsMet() {
        CaseData caseData = CaseDataBuilder.builder()
            .takenOfflineByStaffDate(LocalDateTime.now())
            .caseAccessCategory(UNSPEC_CLAIM)
            .addApplicant2(YES)
            .build();
        assertTrue(getPredicateTakenOfflineByStaffAfterDefendantResponseBeforeClaimantResponse(caseData));
    }

    @Test
    void shouldReturnTrue_whenAllConditionsMetAndApplicant2ResponseDateIsNull() {
        CaseData caseData = CaseDataBuilder.builder()
            .takenOfflineByStaffDate(LocalDateTime.now())
            .caseAccessCategory(UNSPEC_CLAIM)
            .addApplicant2(YES)
            .applicant2ResponseDate(null)
            .build();
        assertTrue(getPredicateTakenOfflineByStaffAfterDefendantResponseBeforeClaimantResponse(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseAccessCategoryIsNotUnspecClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .takenOfflineByStaffDate(LocalDateTime.now())
            .caseAccessCategory(SPEC_CLAIM)
            .build();
        assertTrue(getPredicateTakenOfflineByStaffAfterDefendantResponseBeforeClaimantResponse(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseAccessCategoryIsNotSpecClaim() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .responseClaimTrack(SMALL_CLAIM.name())
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .build();

        assertFalse(allAgreedToLrMediationSpec.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenResponseClaimTrackIsNotSmallClaim() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .responseClaimTrack(FAST_CLAIM.name())
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .build();

        assertFalse(allAgreedToLrMediationSpec.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenResponseClaimMediationSpecRequiredIsNo() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .responseClaimTrack(SMALL_CLAIM.name())
            .responseClaimMediationSpecRequired(YesOrNo.NO)
            .build();

        assertFalse(allAgreedToLrMediationSpec.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenRespondent2ExistsAndNotAgreedToMediationSpec() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .responseClaimTrack(SMALL_CLAIM.name())
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .respondent2(Party.builder().build())
            .respondent2SameLegalRepresentative(YesOrNo.NO)
            .responseClaimMediationSpec2Required(YesOrNo.NO)
            .build();

        assertFalse(allAgreedToLrMediationSpec.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenApplicant1NotAgreedToMediationSpec() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .responseClaimTrack(SMALL_CLAIM.name())
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .applicant1ClaimMediationSpecRequired(SmallClaimMedicalLRspec.builder()
                                                      .hasAgreedFreeMediation(YesOrNo.NO)
                                                      .build())
            .build();

        assertFalse(allAgreedToLrMediationSpec.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenApplicantMPNotAgreedToMediationSpec() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .responseClaimTrack(SMALL_CLAIM.name())
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .applicantMPClaimMediationSpecRequired(SmallClaimMedicalLRspec.builder()
                                                       .hasAgreedFreeMediation(YesOrNo.NO)
                                                       .build())
            .build();

        assertFalse(allAgreedToLrMediationSpec.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
