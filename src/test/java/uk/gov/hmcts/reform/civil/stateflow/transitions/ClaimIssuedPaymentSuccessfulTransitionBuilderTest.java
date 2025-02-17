package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.model.Transition;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimIssuedPaymentSuccessfulTransitionBuilder.multipartyCase;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimIssuedPaymentSuccessfulTransitionBuilder.oneVsOneCase;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimIssuedPaymentSuccessfulTransitionBuilder.pendingClaimIssued;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimIssuedPaymentSuccessfulTransitionBuilder.respondent1NotRepresented;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimIssuedPaymentSuccessfulTransitionBuilder.respondent1OrgNotRegistered;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimIssuedPaymentSuccessfulTransitionBuilder.respondent2NotRepresented;
import static uk.gov.hmcts.reform.civil.stateflow.transitions.ClaimIssuedPaymentSuccessfulTransitionBuilder.respondent2OrgNotRegistered;

@ExtendWith(MockitoExtension.class)
public class ClaimIssuedPaymentSuccessfulTransitionBuilderTest {

    @Mock
    private FeatureToggleService mockFeatureToggleService;

    private List<Transition> result;

    @BeforeEach
    void setUp() {
        ClaimIssuedPaymentSuccessfulTransitionBuilder claimIssuedPaymentSuccessfulTransitionBuilder = new ClaimIssuedPaymentSuccessfulTransitionBuilder(
            mockFeatureToggleService);
        result = claimIssuedPaymentSuccessfulTransitionBuilder.buildTransitions();
        assertNotNull(result);
    }

    @Test
    void shouldSetUpTransitions_withExpectedSizeAndStates() {
        assertThat(result).hasSize(5);

        assertTransition(result.get(0), "MAIN.CLAIM_ISSUED_PAYMENT_SUCCESSFUL", "MAIN.PENDING_CLAIM_ISSUED");
        assertTransition(result.get(1), "MAIN.CLAIM_ISSUED_PAYMENT_SUCCESSFUL", "MAIN.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT");
        assertTransition(result.get(2), "MAIN.CLAIM_ISSUED_PAYMENT_SUCCESSFUL", "MAIN.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT_ONE_V_ONE_SPEC");
        assertTransition(result.get(3), "MAIN.CLAIM_ISSUED_PAYMENT_SUCCESSFUL", "MAIN.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT");
        assertTransition(result.get(4), "MAIN.CLAIM_ISSUED_PAYMENT_SUCCESSFUL", "MAIN.PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT");
    }

    @Test
    void shouldReturnTrue_whenRespondent1IsNotRepresented() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnrepresentedDefendant().build();
        assertTrue(respondent1NotRepresented.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenRespondent1IsRepresentedAndAtPendingClaimIssuedState() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
        assertFalse(respondent1NotRepresented.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenRespondent1IsNotRegistered() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnregisteredDefendant().build();
        assertTrue(respondent1OrgNotRegistered.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenRespondent1IsRegisteredAndAtPendingClaimIssuedState() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
        assertFalse(respondent1OrgNotRegistered.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenRespondent2IsNotRepresented() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnrepresentedDefendant().build();
        assertTrue(respondent2NotRepresented.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenRespondent2IsRepresentedAndAtPendingClaimIssuedState() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
        assertFalse(respondent2NotRepresented.test(caseData));
    }

    @Test
    void shouldReturnTrue_whenRespondent2IsNotRegistered() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnregisteredDefendant().build();
        assertTrue(respondent1OrgNotRegistered.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenRespondent2IsRegisteredAndAtPendingClaimIssuedState() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
        assertFalse(respondent2OrgNotRegistered.test(caseData));
    }

    @Test
    void shouldReturnTrue_forOneVsOneCase() {
        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim().build().toBuilder().build();
        assertTrue(oneVsOneCase.test(caseData));
    }

    @Test
    void shouldReturnTrue_forMultipartyCaseOneVTwoWithTwoReps() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .setClaimTypeToSpecClaim().build().toBuilder().build();
        assertTrue(multipartyCase.test(caseData));
    }

    @Test
    void shouldReturnTrue_forMultipartyCaseOneVTwoWithOneRep() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimSameDefendantSolicitor()
            .setClaimTypeToSpecClaim().build().toBuilder().build();
        assertTrue(multipartyCase.test(caseData));
    }

    @Test
    void shouldReturnTrue_forMultipartyCaseWithTwoApplicants() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .setClaimTypeToSpecClaim().build().toBuilder().build();
        assertTrue(multipartyCase.test(caseData));
    }

    @Test
    public void when1v2ssIssued_thenPendingClaimIssued() {
        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .respondent1Represented(YES)
            .respondent1OrgRegistered(YES)
            .respondent2(Party.builder().build())
            .respondent2Represented(YES)
            .respondent2SameLegalRepresentative(YES)
            .build();

        assertTrue(pendingClaimIssued.test(caseData));
        assertFalse(
            ((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.negate().and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.negate()
                    .and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                .and(ClaimIssuedPaymentSuccessfulTransitionBuilder
                         .bothDefSameLegalRep.negate()).test(caseData));
    }

    @Test
    public void when1v2dsIssued_thenPendingClaimIssued() {
        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .respondent1Represented(YES)
            .respondent1OrgRegistered(YES)
            .respondent2(Party.builder().build())
            .respondent2Represented(YES)
            .respondent2SameLegalRepresentative(NO)
            .respondent2OrgRegistered(YES)
            .build();

        assertTrue(pendingClaimIssued.test(caseData));
        assertFalse(
            ((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.negate().and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.negate().and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                .and(ClaimIssuedPaymentSuccessfulTransitionBuilder
                         .bothDefSameLegalRep.negate()).test(caseData));
    }

    @Test
    public void whenXv1Issued_thenPendingClaimIssued() {
        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .respondent1Represented(YES)
            .respondent1OrgRegistered(YES)
            .build();

        assertTrue(pendingClaimIssued.test(caseData));
        assertFalse(
            ((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.negate().and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.negate().and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.and(
                            respondent2NotRepresented.negate())))
                .and(ClaimIssuedPaymentSuccessfulTransitionBuilder
                         .bothDefSameLegalRep.negate()).test(caseData));
    }

    @Test
    void shouldReturnTrue_whenCaseDataIsAtPendingClaimIssuedState() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
        assertTrue(pendingClaimIssued.test(caseData));
    }

    @Test
    void shouldReturnFalse_whenCaseDataAtDraftState() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful().build();
        assertFalse(pendingClaimIssued.test(caseData));
    }

    private void assertTransition(Transition transition, String sourceState, String targetState) {
        assertThat(transition.getSourceState()).isEqualTo(sourceState);
        assertThat(transition.getTargetState()).isEqualTo(targetState);
    }
}
