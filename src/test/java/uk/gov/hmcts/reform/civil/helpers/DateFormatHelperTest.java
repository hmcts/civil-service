package uk.gov.hmcts.reform.civil.helpers;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

class DateFormatHelperTest {

    @Nested
    class LocalDateTimeFormat {

        @Test
        void shouldReturnExpectedDateTimeFormat_whenValidFormatIsPassed() {
            LocalDateTime now = LocalDateTime.of(2999, 1, 1, 9, 0, 0);

            assertThat(formatLocalDateTime(now, DATE_TIME_AT))
                .isEqualTo("9:00am on 1 January 2999");
        }
    }

    @Nested
    class LocalDateFormat {

        @Test
        void shouldReturnExpectedDateFormat_whenValidFormatIsPassed() {
            LocalDate now = LocalDate.of(2999, 1, 1);

            assertThat(formatLocalDate(now, DATE))
                .isEqualTo("1 January 2999");
        }
    }
}
