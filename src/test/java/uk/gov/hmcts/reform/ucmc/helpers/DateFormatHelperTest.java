package uk.gov.hmcts.reform.ucmc.helpers;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.ucmc.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.ucmc.helpers.DateFormatHelper.formatLocalDateTime;

class DateFormatHelperTest {

    @Test
    void shouldFormatDateToExpectedFormat() {
        LocalDateTime now = LocalDateTime.of(2999, 1, 1, 9, 0,0);

        assertThat(formatLocalDateTime(now, DATE_TIME_AT))
            .isEqualTo("9:00am on 1 January 2999");
    }
}
