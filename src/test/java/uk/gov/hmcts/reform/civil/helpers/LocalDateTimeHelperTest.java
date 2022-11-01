package uk.gov.hmcts.reform.civil.helpers;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalDateTimeHelperTest {

    @Test
    void fromUTC_shouldReturnTimeInLocalZone() {
        // Given
        LocalDateTime farAwayLocalDateTime = LocalDateTime.now(ZoneId.of("America/New_York"));

        // when
        LocalDateTime expectedDateTime = LocalDateTimeHelper.fromUTC(farAwayLocalDateTime);

        // then
        assertThat(farAwayLocalDateTime.atZone(LocalDateTimeHelper.UTC_ZONE).toLocalDateTime())
            .isNotEqualTo(LocalDateTime.now().atZone(LocalDateTimeHelper.UTC_ZONE).toLocalDateTime());
        assertThat(farAwayLocalDateTime.atZone(LocalDateTimeHelper.UTC_ZONE).toLocalDateTime())
            .isEqualTo(expectedDateTime.atZone(LocalDateTimeHelper.UTC_ZONE).toLocalDateTime());
    }
}
