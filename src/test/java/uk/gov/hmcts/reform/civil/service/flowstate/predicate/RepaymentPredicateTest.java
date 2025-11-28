package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepaymentPredicateTest {

    @Mock
    private CaseData caseData;

    @Test
    void should_return_true_for_acceptRepaymentPlan_when_accepted_and_not_lip_case() {
        when(caseData.hasApplicantAcceptedRepaymentPlan()).thenReturn(true);
        when(caseData.isLipvLipOneVOne()).thenReturn(false);
        assertTrue(RepaymentPredicate.acceptRepaymentPlan.test(caseData));
    }

    @Test
    void should_return_true_for_acceptRepaymentPlan_when_accepted_and_lip_case_and_not_taken_offline_by_staff() {
        when(caseData.hasApplicantAcceptedRepaymentPlan()).thenReturn(true);
        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
        assertTrue(RepaymentPredicate.acceptRepaymentPlan.test(caseData));
    }

    @Test
    void should_return_false_for_acceptRepaymentPlan_when_not_accepted() {
        when(caseData.hasApplicantAcceptedRepaymentPlan()).thenReturn(false);
        assertFalse(RepaymentPredicate.acceptRepaymentPlan.test(caseData));
    }

    @Test
    void should_return_false_for_acceptRepaymentPlan_when_accepted_and_lip_case_and_taken_offline_by_staff() {
        when(caseData.hasApplicantAcceptedRepaymentPlan()).thenReturn(true);
        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
        assertFalse(RepaymentPredicate.acceptRepaymentPlan.test(caseData));
    }

    @Test
    void should_return_true_for_rejectRepaymentPlan_when_rejected_and_not_lip_case() {
        when(caseData.hasApplicantRejectedRepaymentPlan()).thenReturn(true);
        when(caseData.isLipvLipOneVOne()).thenReturn(false);
        assertTrue(RepaymentPredicate.rejectRepaymentPlan.test(caseData));
    }

    @Test
    void should_return_true_for_rejectRepaymentPlan_when_rejected_and_lip_case_and_not_taken_offline_by_staff() {
        when(caseData.hasApplicantRejectedRepaymentPlan()).thenReturn(true);
        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(null);
        assertTrue(RepaymentPredicate.rejectRepaymentPlan.test(caseData));
    }

    @Test
    void should_return_false_for_rejectRepaymentPlan_when_not_rejected() {
        when(caseData.hasApplicantRejectedRepaymentPlan()).thenReturn(false);
        assertFalse(RepaymentPredicate.rejectRepaymentPlan.test(caseData));
    }

    @Test
    void should_return_false_for_rejectRepaymentPlan_when_rejected_and_lip_case_and_taken_offline_by_staff() {
        when(caseData.hasApplicantRejectedRepaymentPlan()).thenReturn(true);
        when(caseData.isLipvLipOneVOne()).thenReturn(true);
        when(caseData.getTakenOfflineByStaffDate()).thenReturn(LocalDateTime.now());
        assertFalse(RepaymentPredicate.rejectRepaymentPlan.test(caseData));
    }
}
