package uk.gov.hmcts.reform.civil.model.allowance;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.model.allowance.DisabilityAllowance.getDisabilityAllowance;

class DisabilityAllowanceTest {

    static Stream<Arguments> shouldReturnCalculatedDisabilityAllowance() {
        DisabilityParam severelyDisabledAndCarer = new DisabilityParam(
            false, false, true, false, true
        );
        DisabilityParam severelyDisabledCouple = new DisabilityParam(
            false, true, true, false, false
        );
        DisabilityParam disabledCouple = new DisabilityParam(
            true, true, false, false, false
        );
        DisabilityParam hasDependantDisabledAndIsCarer = new DisabilityParam(
            false, false, false, true, true
        );
        return Stream.of(
            Arguments.of(severelyDisabledAndCarer, 417.94),
            Arguments.of(severelyDisabledCouple, 536.03),
            Arguments.of(disabledCouple, 199.12),
            Arguments.of(hasDependantDisabledAndIsCarer, 410.19),
            Arguments.of(null, 0.0)
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldReturnCalculatedDisabilityAllowance(DisabilityParam disabilityParam, double expectedResult) {
        //When
        double actualResult = getDisabilityAllowance(disabilityParam);
        //Then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

}
