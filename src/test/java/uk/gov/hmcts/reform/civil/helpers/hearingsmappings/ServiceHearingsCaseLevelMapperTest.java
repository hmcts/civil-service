package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceHearingsCaseLevelMapperTest {

    @Nested
    class GetCaseSLAStartDate {
        @Test
        void shouldReturnExpectedSLAStartDateInStringFormat() {
            var date = LocalDate.of(2023, 1, 30);
            var expected = "2023-01-30";

            var actual = ServiceHearingsCaseLevelMapper.getCaseSLAStartDate(date);

            assertThat(actual).isEqualTo(expected);
        }
    }
}
