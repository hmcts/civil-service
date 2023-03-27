package uk.gov.hmcts.reform.civil.helpers;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class LocalDateTimeHelperTest {

    @Test
    void utcTimeShouldBeBehindByAnHour() {
        LocalDateTime utcTime = LocalDateTimeHelper.nowInUTC();
        LocalDateTime localTime = LocalDateTimeHelper.nowInLocalZone();

        long difference = ChronoUnit.HOURS.between(utcTime, localTime);

        assertThat(difference).isEqualTo(1);
    }

    @Test
    void utcTimeToLocalTimeConversionShouldIncrementByAnHour() {
        LocalDateTime utcTime = LocalDateTimeHelper.nowInUTC();

        long difference = ChronoUnit.HOURS.between(utcTime, LocalDateTimeHelper.fromUTC(utcTime));

        assertThat(difference).isEqualTo(1);
    }
}
