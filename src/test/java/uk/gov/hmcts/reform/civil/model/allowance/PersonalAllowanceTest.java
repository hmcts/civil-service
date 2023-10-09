package uk.gov.hmcts.reform.civil.model.allowance;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.model.allowance.PersonalAllowance.COUPLES_OVER_18;
import static uk.gov.hmcts.reform.civil.model.allowance.PersonalAllowance.COUPLES_UNDER_18_OVER_25;
import static uk.gov.hmcts.reform.civil.model.allowance.PersonalAllowance.COUPLES_UNDER_18_UNDER_25;
import static uk.gov.hmcts.reform.civil.model.allowance.PersonalAllowance.SINGLE_OVER_25;
import static uk.gov.hmcts.reform.civil.model.allowance.PersonalAllowance.SINGLE_UNDER_25;

class PersonalAllowanceTest {

    static Stream<Arguments> shouldReturnCorrespondingEnum() {
        return Stream.of(
            Arguments.of(24, false, false, SINGLE_UNDER_25),
            Arguments.of(26, false, false, SINGLE_OVER_25),
            Arguments.of(24, true, false, COUPLES_UNDER_18_UNDER_25),
            Arguments.of(31, true, false, COUPLES_UNDER_18_OVER_25),
            Arguments.of(30, true, true, COUPLES_OVER_18)
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldReturnCorrespondingEnum(int age, boolean hasPartner, boolean partnerUnder18, PersonalAllowance expectedResult) {
        //When
        PersonalAllowance personalAllowance = PersonalAllowance.getPersonalAllowance(age, hasPartner, partnerUnder18);
        //Then
        assertThat(personalAllowance).isEqualTo(expectedResult);
    }

}
