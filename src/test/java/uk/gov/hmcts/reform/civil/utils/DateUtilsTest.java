package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class DateUtilsTest {

    @Test
    public void testConvertFromUTC_withBSTDate() {
        LocalDateTime utcDate = LocalDateTime.of(2022, 7, 28, 9, 00, 00);
        LocalDateTime ukDate = DateUtils.convertFromUTC(utcDate);
        Assertions.assertEquals("2022-07-28T10:00", ukDate.toString());
    }

    @Test
    public void testConvertFromUTC_GMTDate() {
        LocalDateTime utcDate = LocalDateTime.of(2022, 12, 28, 9, 00, 00);
        LocalDateTime ukDate = DateUtils.convertFromUTC(utcDate);
        Assertions.assertEquals("2022-12-28T09:00", ukDate.toString());
    }
}
