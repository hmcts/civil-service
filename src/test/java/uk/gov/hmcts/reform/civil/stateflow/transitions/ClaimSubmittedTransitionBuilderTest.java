package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LanguagePredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.LipPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.PaymentPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.TakenOfflinePredicate;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ClaimSubmittedTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        ClaimSubmittedTransitionBuilder claimSubmittedTransitionBuilder = new ClaimSubmittedTransitionBuilder(
            mockFeatureToggleService);
        result = claimSubmittedTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions() {
        assertThat(result).hasSize(8);

        assertTransition(result.get(0), "MAIN.CLAIM_SUBMITTED", "MAIN.CLAIM_ISSUED_PAYMENT_SUCCESSFUL");
        assertTransition(result.get(1), "MAIN.CLAIM_SUBMITTED", "MAIN.TAKEN_OFFLINE_BY_STAFF");
        assertTransition(result.get(2), "MAIN.CLAIM_SUBMITTED", "MAIN.CLAIM_ISSUED_PAYMENT_FAILED");
        assertTransition(result.get(3), "MAIN.CLAIM_SUBMITTED", "MAIN.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC");
        assertTransition(result.get(4), "MAIN.CLAIM_SUBMITTED", "MAIN.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC");
        assertTransition(result.get(5), "MAIN.CLAIM_SUBMITTED", "MAIN.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC");
        assertTransition(result.get(6), "MAIN.CLAIM_SUBMITTED", "MAIN.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC");
        assertTransition(result.get(7), "MAIN.CLAIM_SUBMITTED", "MAIN.SPEC_DEFENDANT_NOC");
    }

    @Test
    void shouldGoNocRoute_whenDefendantNoCOnline() {
        ClaimSubmittedTransitionBuilder claimSubmittedTransitionBuilder = new ClaimSubmittedTransitionBuilder(
            mockFeatureToggleService);
        result = claimSubmittedTransitionBuilder.buildTransitions();

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedPaymentFailed().build();
        assertTrue(PaymentPredicate.failed.test(caseData));
        assertThat(getCaseFlags(result.get(3), caseData)).hasSize(2).contains(
            entry(FlowFlag.LIP_CASE.name(), true),
            entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true)
        );
    }

    @Test
    void shouldSetBilingualFlag_whenDefendantNoCOnline() {
        ClaimSubmittedTransitionBuilder claimSubmittedTransitionBuilder = new ClaimSubmittedTransitionBuilder(
            mockFeatureToggleService);
        result = claimSubmittedTransitionBuilder.buildTransitions();

        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        caseData = caseData.toBuilder().claimantBilingualLanguagePreference("BOTH").build();
        assertThat(getCaseFlags(result.get(6), caseData)).hasSize(3).contains(
            entry(FlowFlag.LIP_CASE.name(), true),
            entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false),
            entry(FlowFlag.CLAIM_ISSUE_BILINGUAL.name(), true)
        );
    }

    @Test
    void shouldSetHwfFlag_whenLiPvLiP_andHelpWithFees() {
        // Transition index 3: LiP v LiP path (not taken offline) sets flags and conditionally HWF/Bilingual
        ClaimSubmittedTransitionBuilder claimSubmittedTransitionBuilder = new ClaimSubmittedTransitionBuilder(
            mockFeatureToggleService);
        result = claimSubmittedTransitionBuilder.buildTransitions();

        // Build case with Help With Fees = YES and no bilingual preference
        CaseData caseData = buildCaseDataWithHelpWithFees(YesOrNo.YES);
        Map<String, Boolean> flags = getCaseFlags(result.get(3), caseData);
        assertThat(flags).hasSizeGreaterThanOrEqualTo(3).contains(
            entry(FlowFlag.LIP_CASE.name(), true),
            entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true),
            entry(FlowFlag.CLAIM_ISSUE_HWF.name(), true)
        );
    }

    @Test
    void shouldSetFlags_whenNocAppliedForLiPClaimant() {
        // Transition index 4: nocApplyForLiPClaimant path
        ClaimSubmittedTransitionBuilder claimSubmittedTransitionBuilder = new ClaimSubmittedTransitionBuilder(
            mockFeatureToggleService);
        result = claimSubmittedTransitionBuilder.buildTransitions();

        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        assertThat(getCaseFlags(result.get(4), caseData)).hasSize(2).contains(
            entry(FlowFlag.LIP_CASE.name(), false),
            entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true)
        );
    }

    @Test
    void shouldSetFlags_whenLiPvLR_NoNocSubmittedAndFeatureOff() {
        // Transition index 5: LiP v LR, NOC feature OFF and no NOC submitted
        ClaimSubmittedTransitionBuilder claimSubmittedTransitionBuilder = new ClaimSubmittedTransitionBuilder(
            mockFeatureToggleService);
        result = claimSubmittedTransitionBuilder.buildTransitions();

        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        assertThat(getCaseFlags(result.get(5), caseData)).hasSize(2).contains(
            entry(FlowFlag.LIP_CASE.name(), true),
            entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false)
        );
    }

    @Test
    void shouldSetFlags_whenSpecDefendantNocPath() {
        // Transition index 7: SPEC_DEFENDANT_NOC path sets LIP flags
        ClaimSubmittedTransitionBuilder claimSubmittedTransitionBuilder = new ClaimSubmittedTransitionBuilder(
            mockFeatureToggleService);
        result = claimSubmittedTransitionBuilder.buildTransitions();

        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        assertThat(getCaseFlags(result.get(7), caseData)).hasSize(2).contains(
            entry(FlowFlag.LIP_CASE.name(), true),
            entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), false)
        );
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtIssuedStateClaimIssuedPayment() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedPaymentFailed().build();
        assertTrue(PaymentPredicate.failed.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtIssuedState() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentFailed().build();
        assertTrue(PaymentPredicate.failed.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataIsAtDraftState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        assertFalse(PaymentPredicate.failed.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataIsBilingual() {
        CaseData caseData = CaseData.builder()
            .claimantBilingualLanguagePreference("BOTH")
            .build();
        assertTrue(LanguagePredicate.claimantIsBilingual.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataIsNotBilingual() {
        CaseData caseData = CaseData.builder()
            .claimantBilingualLanguagePreference(null)
            .build();
        assertFalse(LanguagePredicate.claimantIsBilingual.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenHelpWithFeeIsYes() {
        CaseData caseData = buildCaseDataWithHelpWithFees(YesOrNo.YES);
        assertTrue(LipPredicate.isHelpWithFees.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenHelpWithFeeIsNo() {
        CaseData caseData = buildCaseDataWithHelpWithFees(YesOrNo.NO);
        assertFalse(LipPredicate.isHelpWithFees.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenHelpWithFeeIsNull() {
        CaseData caseData = buildCaseDataWithHelpWithFees(null);
        assertFalse(LipPredicate.isHelpWithFees.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseTakenOfflineBeforeIssue() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
            .takenOfflineByStaff()
            .build();
        Assertions.assertTrue(TakenOfflinePredicate.byStaff.and(TakenOfflinePredicate.beforeClaimIssue).test(caseData));
    }

    private CaseData buildCaseDataWithHelpWithFees(YesOrNo helpWithFee) {
        HelpWithFees helpWithFees = new HelpWithFees()
            .setHelpWithFee(helpWithFee);

        CaseDataLiP caseDataLiP = new CaseDataLiP()
            .setHelpWithFees(helpWithFees);

        return CaseData.builder()
            .caseDataLiP(caseDataLiP)
            .build();
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }

    private Map<String, Boolean> getCaseFlags(Transition result, CaseData caseData) {
        Map<String, Boolean> flags = new HashMap<>();
        if (result.getDynamicFlags() != null) {
            // Prefer dynamic flags (BiConsumer<CaseData, Map<...>>) when present
            result.getDynamicFlags().accept(caseData, flags);
        } else if (result.getFlags() != null) {
            // Fall back to static flags (Consumer<Map<...>>) when dynamic flags are not set
            result.getFlags().accept(flags);
        }
        return flags;
    }
}
