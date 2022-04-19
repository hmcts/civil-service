package uk.gov.hmcts.reform.civil.helpers;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LocalDateTimeHelperTest {

    @Test
    void utcTimeShouldBeBehindByAnHour() {
        LocalDateTime utcTime = LocalDateTimeHelper.nowInUTC();
        LocalDateTime localTime = LocalDateTimeHelper.nowInLocalZone();

        assertThat(localTime.getHour() - utcTime.getHour()).isEqualTo(1);
    }

    @Test
    void utcTimeToLocalTimeConversionShouldIncrementByAnHour() {
        LocalDateTime utcTime = LocalDateTimeHelper.nowInUTC();

        assertThat(LocalDateTimeHelper.fromUTC(utcTime).getHour() - utcTime.getHour()).isEqualTo(1);
    }
}
