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
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimSubmittedTransitionBuilder.claimIssueBilingual;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimSubmittedTransitionBuilder.claimIssueHwF;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimSubmittedTransitionBuilder.paymentFailed;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimSubmittedTransitionBuilder.takenOfflineByStaffBeforeClaimIssued;

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
        when(mockFeatureToggleService.isPinInPostEnabled()).thenReturn(true);
        ClaimSubmittedTransitionBuilder claimSubmittedTransitionBuilder = new ClaimSubmittedTransitionBuilder(
            mockFeatureToggleService);
        result = claimSubmittedTransitionBuilder.buildTransitions();

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedPaymentFailed().build();
        assertTrue(paymentFailed.test(caseData));
        assertThat(getCaseFlags(result.get(3), caseData)).hasSize(3).contains(
            entry(FlowFlag.LIP_CASE.name(), true),
            entry(FlowFlag.UNREPRESENTED_DEFENDANT_ONE.name(), true),
            entry(FlowFlag.PIP_ENABLED.name(), true)
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
    void shouldReturnTrue_whenCaseDataAtIssuedStateClaimIssuedPayment() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssuedPaymentFailed().build();
        assertTrue(paymentFailed.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataAtIssuedState() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentFailed().build();
        assertTrue(paymentFailed.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataIsAtDraftState() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();
        assertFalse(paymentFailed.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataIsBilingual() {
        CaseData caseData = CaseData.builder()
            .claimantBilingualLanguagePreference("BOTH")
            .build();
        assertTrue(claimIssueBilingual.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataIsNotBilingual() {
        CaseData caseData = CaseData.builder()
            .claimantBilingualLanguagePreference(null)
            .build();
        assertFalse(claimIssueBilingual.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenHelpWithFeeIsYes() {
        CaseData caseData = buildCaseDataWithHelpWithFees(YesOrNo.YES);
        assertTrue(claimIssueHwF.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenHelpWithFeeIsNo() {
        CaseData caseData = buildCaseDataWithHelpWithFees(YesOrNo.NO);
        assertFalse(claimIssueHwF.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenHelpWithFeeIsNull() {
        CaseData caseData = buildCaseDataWithHelpWithFees(null);
        assertFalse(claimIssueHwF.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseTakenOfflineBeforeIssue() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
            .takenOfflineByStaff()
            .build();
        Assertions.assertTrue(takenOfflineByStaffBeforeClaimIssued.test(caseData));
    }

    private CaseData buildCaseDataWithHelpWithFees(YesOrNo helpWithFee) {
        HelpWithFees helpWithFees = HelpWithFees.builder()
            .helpWithFee(helpWithFee)
            .build();

        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .helpWithFees(helpWithFees)
            .build();

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
        result.getDynamicFlags().accept(caseData, flags);
        return flags;
    }
}
