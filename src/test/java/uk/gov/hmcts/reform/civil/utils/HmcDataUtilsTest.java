package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingDay;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HmcDataUtilsTest {

    @Nested
    class GetHearingStartDay {

        @Test
        void shouldReturnTheExpectedFirstDay() {
            var expected = HearingDaySchedule.builder()
                .hearingStartDateTime(LocalDateTime.of(2023, 01, 01, 0, 0, 0))
                .build();

            var hearing = HearingGetResponse.builder()
                .hearingResponse(
                    HearingResponse.builder()
                        .hearingDaySchedule(List.of(
                            HearingDaySchedule.builder()
                                .hearingStartDateTime(
                                    LocalDateTime.of(2023, 01, 03, 0, 0, 0))
                                .build(),
                            expected,
                            HearingDaySchedule.builder()
                                .hearingStartDateTime(
                                    LocalDateTime.of(2023, 01, 02, 0, 0, 0))
                                .build()
                        ))
                        .build())
                .build();

            assertEquals(HmcDataUtils.getHearingStartDay(hearing), expected);
        }

        @Test
        void shouldReturnNullIfHearingIsNull() {
            assertNull(HmcDataUtils.getHearingStartDay(null));
        }

        @Test
        void shouldReturnNullIfHearingResponseIsNull() {
            var hearing = HearingGetResponse.builder().build();
            assertNull(HmcDataUtils.getHearingStartDay(hearing));
        }

        @Test
        void shouldReturnNullIfHearingResponseScheduleDaysAreNull() {
            var hearing = HearingGetResponse.builder()
                .hearingResponse(HearingResponse.builder().build())
                .build();
            assertNull(HmcDataUtils.getHearingStartDay(hearing));
        }

        @Test
        void shouldReturnNullIfHearingResponseScheduleDaysAreEmpty() {
            var hearing = HearingGetResponse.builder()
                .hearingResponse(
                    HearingResponse.builder().hearingDaySchedule(new ArrayList<>()).build())
                .build();
            assertNull(HmcDataUtils.getHearingStartDay(hearing));
        }
    }

    @Nested
    class GetHearingDays {

        @Test
        void shouldReturnExpectedHearingDays() {
            var hearingDayOne = HearingDay.builder()
                .hearingStartDateTime(
                    LocalDateTime.of(2023, 01, 03, 0, 0, 0))
                .hearingEndDateTime(
                    LocalDateTime.of(2023, 01, 03, 12, 0, 0))
                .build();

            var hearingDayTwo = HearingDay.builder()
                .hearingStartDateTime(
                    LocalDateTime.of(2023, 01, 05, 0, 0, 0))
                .hearingEndDateTime(
                    LocalDateTime.of(2023, 01, 05, 12, 0, 0))
                .build();

            var hearing = HearingGetResponse.builder()
                .hearingResponse(
                    HearingResponse.builder()
                        .hearingDaySchedule(List.of(
                            HearingDaySchedule.builder()
                                .hearingStartDateTime(hearingDayOne.getHearingStartDateTime())
                                .hearingEndDateTime(hearingDayOne.getHearingEndDateTime())
                                .build(),
                            HearingDaySchedule.builder()
                                .hearingStartDateTime(hearingDayTwo.getHearingStartDateTime())
                                .hearingEndDateTime(hearingDayTwo.getHearingEndDateTime())
                                .build()
                        ))
                        .build())
                .build();

            assertEquals(HmcDataUtils.getHearingDays(hearing), List.of(hearingDayOne, hearingDayTwo));
        }
    }
}
