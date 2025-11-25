package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentPredicateTest {

    @Mock
    private CaseData caseData;

    @Test
    void should_return_true_for_successful_when_represented_and_payment_successful_date_exists() {
        when(caseData.isApplicantNotRepresented()).thenReturn(false);
        when(caseData.getPaymentSuccessfulDate()).thenReturn(LocalDateTime.now());
        assertTrue(PaymentPredicate.successful.test(caseData));
    }

    @Test
    void should_return_true_for_successful_when_represented_and_claim_issued_payment_succeeded() {
        when(caseData.isApplicantNotRepresented()).thenReturn(false);
        when(caseData.getClaimIssuedPaymentDetails()).thenReturn(PaymentDetails.builder().status(PaymentStatus.SUCCESS).build());
        assertTrue(PaymentPredicate.successful.test(caseData));
    }

    @Test
    void should_return_false_for_successful_when_not_represented() {
        when(caseData.isApplicantNotRepresented()).thenReturn(true);
        assertFalse(PaymentPredicate.successful.test(caseData));
    }

    @Test
    void should_return_false_for_successful_when_no_payment_info() {
        when(caseData.isApplicantNotRepresented()).thenReturn(false);
        when(caseData.getPaymentSuccessfulDate()).thenReturn(null);
        when(caseData.getClaimIssuedPaymentDetails()).thenReturn(null);
        assertFalse(PaymentPredicate.successful.test(caseData));
    }

    @Test
    void should_return_false_for_successful_when_payment_failed() {
        when(caseData.isApplicantNotRepresented()).thenReturn(false);
        when(caseData.getPaymentSuccessfulDate()).thenReturn(null);
        when(caseData.getClaimIssuedPaymentDetails()).thenReturn(PaymentDetails.builder().status(PaymentStatus.FAILED).build());
        assertFalse(PaymentPredicate.successful.test(caseData));
    }
}
