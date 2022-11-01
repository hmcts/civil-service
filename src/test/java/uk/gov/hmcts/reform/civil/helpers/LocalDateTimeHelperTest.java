package uk.gov.hmcts.reform.civil.helpers;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class LocalDateTimeHelperTest {

    @Test
    void utcTimeShouldBeBehindByAnHour() {
        LocalDateTime utcTime = LocalDateTimeHelper.nowInUTC();
        LocalDateTime localTime = LocalDateTimeHelper.nowInLocalZone();
        TimeZone localTimeZone = TimeZone.getTimeZone(LocalDateTimeHelper.LOCAL_ZONE);

        long difference = ChronoUnit.HOURS.between(utcTime, localTime);
        if (localTimeZone.inDaylightTime(Date
                                             .from(localTime.atZone(LocalDateTimeHelper.LOCAL_ZONE)
                                                       .toInstant()))) {
            assertThat(difference).isEqualTo(1);
        } else {
            assertThat(difference).isEqualTo(0);
        }
    }

    @Test
    void utcTimeToLocalTimeConversionShouldIncrementByAnHour() {
        LocalDateTime utcTime = LocalDateTimeHelper.nowInUTC();
        LocalDateTime localTime = LocalDateTimeHelper.fromUTC(utcTime);
        long difference = ChronoUnit.HOURS.between(utcTime, localTime);
        TimeZone localTimeZone = TimeZone.getTimeZone(LocalDateTimeHelper.LOCAL_ZONE);

        if (localTimeZone.inDaylightTime(Date
                                             .from(localTime.atZone(LocalDateTimeHelper.LOCAL_ZONE)
                                                       .toInstant()))) {
            assertThat(difference).isEqualTo(1);
        } else {
            assertThat(difference).isEqualTo(0);
        }
    }
}
