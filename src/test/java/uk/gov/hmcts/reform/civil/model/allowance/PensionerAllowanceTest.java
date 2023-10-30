package uk.gov.hmcts.reform.civil.model.allowance;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.model.allowance.PensionerAllowance.getPensionerAllowance;

class PensionerAllowanceTest {

    static Stream<Arguments> shouldReturnPensionerAllowance() {
        Arguments singlePensioner = Arguments.of(true, false, 335.83);
        Arguments partnerPensioner = Arguments.of(false, true, 335.83);
        Arguments couplePensioners = Arguments.of(true, true, 502.66);
        Arguments notPensioner = Arguments.of(false, false, 0.0);
        return Stream.of(
            singlePensioner,
            partnerPensioner,
            couplePensioners,
            notPensioner
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldReturnPensionerAllowance(boolean pensioner, boolean partnerPensioner, double expectedResult) {
        //When
        double actualResult = getPensionerAllowance(pensioner, partnerPensioner);
        //Then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

}
