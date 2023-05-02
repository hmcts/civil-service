package uk.gov.hmcts.reform.civil.service.flowstate.transitions;

import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.bothDefSameLegalRep;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent1NotRepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent1OrgNotRegistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent2NotRepresented;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondent2OrgNotRegistered;
import static uk.gov.hmcts.reform.civil.service.flowstate.transitions.ClaimIssuedPaymentSuccessfulTransitions.pendingClaimIssued;

class ClaimIssuedPaymentSuccessfulTransitionsTest {
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

    @Test
    void testDisjoint() {
        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .respondent1Represented(YES)
            .respondent1OrgRegistered(YES)
            .addRespondent2(NO)
            .build();

        Assertions.assertTrue(pendingClaimIssued.test(caseData));

        caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .respondent1Represented(YES)
            .respondent1OrgRegistered(YES)
            .addRespondent2(YES)
            .respondent2Represented(YES)
            .respondent2OrgRegistered(YES)
            .build();

        Assertions.assertTrue(pendingClaimIssued.test(caseData));
    }

    @Test
    void when1v2ssIssued_thenPendingClaimIssued() {
        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .respondent1Represented(YES)
            .respondent1OrgRegistered(YES)
            .respondent2(Party.builder().build())
            .respondent2Represented(YES)
            .respondent2SameLegalRepresentative(YES)
            .build();

        Assertions.assertTrue(pendingClaimIssued.test(caseData));
        Assertions.assertFalse(
            ((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.negate().and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.negate().and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                .and(bothDefSameLegalRep.negate()).test(caseData));
    }

    @Test
    void when1v2dsIssued_thenPendingClaimIssued() {
        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .respondent1Represented(YES)
            .respondent1OrgRegistered(YES)
            .respondent2(Party.builder().build())
            .respondent2Represented(YES)
            .respondent2SameLegalRepresentative(NO)
            .respondent2OrgRegistered(YES)
            .build();

        Assertions.assertTrue(pendingClaimIssued.test(caseData));
        Assertions.assertFalse(
            ((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.negate().and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.negate().and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                .and(bothDefSameLegalRep.negate()).test(caseData));
    }

    @Test
    void whenXv1Issued_thenPendingClaimIssued() {
        CaseData caseData = CaseData.builder()
            .issueDate(LocalDate.now())
            .respondent1Represented(YES)
            .respondent1OrgRegistered(YES)
            .build();

        Assertions.assertTrue(pendingClaimIssued.test(caseData));
        Assertions.assertFalse(
            ((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.negate().and(respondent2NotRepresented.negate())))
                .or((respondent1OrgNotRegistered.negate().and(respondent1NotRepresented.negate()))
                        .and(respondent2OrgNotRegistered.and(respondent2NotRepresented.negate())))
                .and(bothDefSameLegalRep.negate()).test(caseData));
    }

}
