package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
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

    @Nested
    class PaymentSuccessful {

        @Test
        void should_return_true_for_successful_when_represented_and_payment_successful_date_exists() {
            when(caseData.isApplicantNotRepresented()).thenReturn(false);
            when(caseData.getPaymentSuccessfulDate()).thenReturn(LocalDateTime.now());
            assertTrue(PaymentPredicate.successful.test(caseData));
        }

        @Test
        void should_return_true_for_successful_when_represented_and_claim_issued_payment_succeeded() {
            when(caseData.isApplicantNotRepresented()).thenReturn(false);
            when(caseData.getClaimIssuedPaymentDetails()).thenReturn(new PaymentDetails().setStatus(PaymentStatus.SUCCESS));
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
            when(caseData.getClaimIssuedPaymentDetails()).thenReturn(new PaymentDetails().setStatus(PaymentStatus.FAILED));
            assertFalse(PaymentPredicate.successful.test(caseData));
        }
    }

    @Nested
    class PaymentFailed {

        @Test
        void should_return_true_for_failed_when_represented_and_payment_details_failed() {
            when(caseData.isApplicantNotRepresented()).thenReturn(false);
            when(caseData.getPaymentDetails()).thenReturn(new PaymentDetails().setStatus(PaymentStatus.FAILED));
            assertTrue(PaymentPredicate.failed.test(caseData));
        }

        @Test
        void should_return_true_for_failed_when_represented_and_claim_issued_payment_failed() {
            when(caseData.isApplicantNotRepresented()).thenReturn(false);
            when(caseData.getClaimIssuedPaymentDetails()).thenReturn(new PaymentDetails().setStatus(PaymentStatus.FAILED));
            assertTrue(PaymentPredicate.failed.test(caseData));
        }

        @Test
        void should_return_false_for_failed_when_not_represented() {
            when(caseData.isApplicantNotRepresented()).thenReturn(true);
            assertFalse(PaymentPredicate.failed.test(caseData));
        }

        @Test
        void should_return_false_for_failed_when_no_failed_payment_info() {
            when(caseData.isApplicantNotRepresented()).thenReturn(false);
            when(caseData.getPaymentDetails()).thenReturn(null);
            when(caseData.getClaimIssuedPaymentDetails()).thenReturn(null);
            assertFalse(PaymentPredicate.failed.test(caseData));
        }
    }

    @Nested
    class PayImmediately {

        @Test
        void should_return_true_for_payImmediatelyPartAdmission_when_pay_immediately_is_selected() {
            when(caseData.isPayImmediately()).thenReturn(true);
            assertTrue(PaymentPredicate.payImmediatelyPartAdmit.test(caseData));
        }

        @Test
        void should_return_false_for_payImmediatelyPartAdmission_when_pay_immediately_is_not_selected() {
            when(caseData.isPayImmediately()).thenReturn(false);
            assertFalse(PaymentPredicate.payImmediatelyPartAdmit.test(caseData));
        }

        @Test
        void should_return_true_for_payImmediatelyFullAdmission_when_spec_1v1_when_to_be_paid_present_and_no_proceed_decision() {
            when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
            when(caseData.getWhenToBePaidText()).thenReturn("Tomorrow");
            assertTrue(PaymentPredicate.payImmediatelyFullAdmission.test(caseData));
        }

        @Test
        void should_return_false_for_payImmediatelyFullAdmission_when_not_spec() {
            when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.UNSPEC_CLAIM);
            assertFalse(PaymentPredicate.payImmediatelyFullAdmission.test(caseData));
        }

        @Test
        void should_return_false_for_payImmediatelyFullAdmission_when_when_to_be_paid_missing() {
            when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
            when(caseData.getWhenToBePaidText()).thenReturn(null);
            assertFalse(PaymentPredicate.payImmediatelyFullAdmission.test(caseData));
        }

        @Test
        void should_return_false_for_payImmediatelyFullAdmission_when_proceed_decision_present() {
            when(caseData.getCaseAccessCategory()).thenReturn(CaseCategory.SPEC_CLAIM);
            when(caseData.getWhenToBePaidText()).thenReturn("Next week");
            when(caseData.getApplicant1ProceedWithClaim()).thenReturn(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO);
            assertFalse(PaymentPredicate.payImmediatelyFullAdmission.test(caseData));
        }
    }

}
