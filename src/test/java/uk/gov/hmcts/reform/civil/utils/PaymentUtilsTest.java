package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentUtilsTest {

    @Test
    void shouldReturnTrue_whenPaymentIsAlreadyApplied() {
        PaymentDetails intendedPayment = new PaymentDetails()
            .setReference("REF1")
            .setStatus(PaymentStatus.SUCCESS);
        PaymentDetails freshPayment = new PaymentDetails()
            .setReference("REF1")
            .setStatus(PaymentStatus.SUCCESS);

        assertThat(PaymentUtils.isPaymentAlreadyApplied(intendedPayment, freshPayment)).isTrue();
    }

    @Test
    void shouldReturnFalse_whenReferenceMismatch() {
        PaymentDetails intendedPayment = new PaymentDetails()
            .setReference("REF1")
            .setStatus(PaymentStatus.SUCCESS);
        PaymentDetails freshPayment = new PaymentDetails()
            .setReference("REF2")
            .setStatus(PaymentStatus.SUCCESS);

        assertThat(PaymentUtils.isPaymentAlreadyApplied(intendedPayment, freshPayment)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenStatusIsNotSuccess() {
        PaymentDetails intendedPayment = new PaymentDetails()
            .setReference("REF1")
            .setStatus(PaymentStatus.SUCCESS);
        PaymentDetails freshPayment = new PaymentDetails()
            .setReference("REF1")
            .setStatus(PaymentStatus.FAILED);

        assertThat(PaymentUtils.isPaymentAlreadyApplied(intendedPayment, freshPayment)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenFreshPaymentIsNull() {
        PaymentDetails intendedPayment = new PaymentDetails()
            .setReference("REF1")
            .setStatus(PaymentStatus.SUCCESS);

        assertThat(PaymentUtils.isPaymentAlreadyApplied(intendedPayment, null)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenIntendedPaymentIsNull() {
        PaymentDetails freshPayment = new PaymentDetails()
            .setReference("REF1")
            .setStatus(PaymentStatus.SUCCESS);

        assertThat(PaymentUtils.isPaymentAlreadyApplied(null, freshPayment)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenBothReferencesAreNull() {
        PaymentDetails intendedPayment = new PaymentDetails()
            .setReference(null)
            .setStatus(PaymentStatus.SUCCESS);
        PaymentDetails freshPayment = new PaymentDetails()
            .setReference(null)
            .setStatus(PaymentStatus.SUCCESS);

        assertThat(PaymentUtils.isPaymentAlreadyApplied(intendedPayment, freshPayment)).isFalse();
    }
}
