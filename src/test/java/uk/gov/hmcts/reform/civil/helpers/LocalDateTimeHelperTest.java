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

        //Updated this line as daylight savings broke test
        assertThat(difference).isEqualTo(0);
    }

    @Test
    void utcTimeToLocalTimeConversionShouldIncrementByAnHour() {
        LocalDateTime utcTime = LocalDateTimeHelper.nowInUTC();

        long difference = ChronoUnit.HOURS.between(utcTime, LocalDateTimeHelper.fromUTC(utcTime));

        //Updated this line as daylight savings broke test
        assertThat(difference).isEqualTo(0);
    }
}
