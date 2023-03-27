package uk.gov.hmcts.reform.civil.helpers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalDateTimeHelperTest {

    @Test
    void fromUTC_shouldReturnTimeInLocalZoneWinterTime() {
        // Given
        LocalDateTime farAwayLocalDateTime = LocalDateTime.of(2022,12,30,12,30,5)
            .atZone(ZoneId.of("America/New_York")).toLocalDateTime();

        // when
        LocalDateTime expectedDateTime = LocalDateTimeHelper.fromUTC(farAwayLocalDateTime);

        // then
        assertThat(farAwayLocalDateTime.atZone(LocalDateTimeHelper.UTC_ZONE).toLocalDateTime())
            .isEqualTo(expectedDateTime.atZone(LocalDateTimeHelper.UTC_ZONE).toLocalDateTime());
    }

    @Test
    void fromUTC_shouldReturnTimeInLocalZoneSummerTime() {
        // Given
        LocalDateTime farAwayLocalDateTime = LocalDateTime.of(2022,6,30,12,30,5)
            .atZone(ZoneId.of("America/New_York")).toLocalDateTime();

        // when
        LocalDateTime expectedDateTime = LocalDateTimeHelper.fromUTC(farAwayLocalDateTime);

        // then
        assertThat(farAwayLocalDateTime.atZone(LocalDateTimeHelper.UTC_ZONE).toLocalDateTime())
            .isEqualTo(expectedDateTime.atZone(LocalDateTimeHelper.UTC_ZONE).minus(1, ChronoUnit.HOURS).toLocalDateTime());
    }
}
