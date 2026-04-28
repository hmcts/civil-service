package uk.gov.hmcts.reform.civil.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentStatusTest {

    @ParameterizedTest
    @CsvSource({
        "success, SUCCESS",
        "failed, FAILED"})
    void shouldResolvePaymentStatus(String status, PaymentStatus expectedStatus) {
        assertThat(PaymentStatus.resolvePaymentStatus(status)).isEqualTo(expectedStatus);
    }

    @Test
    void shouldThrowExceptionForInvalidPaymentStatus() {
        assertThatThrownBy(() -> PaymentStatus.resolvePaymentStatus("invalid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid payment status: invalid");

        assertThatThrownBy(() -> PaymentStatus.resolvePaymentStatus(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid payment status: null");

        assertThatThrownBy(() -> PaymentStatus.resolvePaymentStatus(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid payment status: ");
    }

    @ParameterizedTest
    @CsvSource({"Success", "Failed"})
    void shouldReturnTrueIfValidStatus(String status) {
        assertThat(PaymentStatus.isValid(status)).isTrue();
    }

    @Test
    void shouldReturnFalseIfInvalidStatus() {
        assertThat(PaymentStatus.isValid("initiated")).isFalse();
        assertThat(PaymentStatus.isValid(null)).isFalse();
        assertThat(PaymentStatus.isValid("")).isFalse();
    }
}
