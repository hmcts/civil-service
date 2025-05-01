package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec.ONCE_FOUR_WEEKS;
import static uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec.ONCE_ONE_WEEK;
import static uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec.ONCE_THREE_WEEKS;
import static uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec.ONCE_TWO_WEEKS;
import static uk.gov.hmcts.reform.civil.utils.PaymentFrequencyCalculator.calculatePaymentPerMonth;

class PaymentFrequencyCalculatorTest {

    private static double EXPECTED_RESULT = 433.0;

    static Stream<Arguments> shouldCalculatePaymentFrequencyPerMonth() {
        return Stream.of(
            Arguments.of(ONCE_ONE_WEEK, 100.0, EXPECTED_RESULT),
            Arguments.of(ONCE_TWO_WEEKS, 200.0, EXPECTED_RESULT),
            Arguments.of(ONCE_THREE_WEEKS, 300.0, EXPECTED_RESULT),
            Arguments.of(ONCE_FOUR_WEEKS, 400.0, EXPECTED_RESULT)
        );

    }

    @ParameterizedTest
    @MethodSource
    void shouldCalculatePaymentFrequencyPerMonth(PaymentFrequencyLRspec paymentFrequency, double amount, double expectedResult) {
        //When
        double actualResult = calculatePaymentPerMonth(amount, paymentFrequency);
        //Then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

}
