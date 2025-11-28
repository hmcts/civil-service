package uk.gov.hmcts.reform.civil.postcode;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CountriesAllowedTest {

    @Test
    void shouldExposeAllSupportedCountries() {
        assertThat(CountriesAllowed.values())
            .containsExactlyInAnyOrder(
                CountriesAllowed.ENGLAND,
                CountriesAllowed.SCOTLAND,
                CountriesAllowed.WALES,
                CountriesAllowed.NOT_FOUND
            );
    }
}
