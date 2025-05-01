package uk.gov.hmcts.reform.civil.model.allowance;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.model.allowance.DisabilityAllowance.getDisabilityAllowance;

class DisabilityAllowanceTest {

    static Stream<Arguments> shouldReturnCalculatedDisabilityAllowance() {
        DisabilityParam severelyDisabledAndCarer = DisabilityParam.builder()
            .carer(true)
            .severelyDisabled(true)
            .build();
        DisabilityParam severelyDisabledCouple = DisabilityParam.builder()
            .severelyDisabled(true)
            .hasPartner(true)
            .build();
        DisabilityParam disabledCouple = DisabilityParam.builder()
            .disabled(true)
            .hasPartner(true)
            .build();
        DisabilityParam hasDependantDisabledAndIsCarer = DisabilityParam.builder()
            .dependant(true)
            .carer(true)
            .build();
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
