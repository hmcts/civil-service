package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingDay;
import uk.gov.hmcts.reform.hmc.model.hearing.CaseDetailsHearing;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingRequestDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedServiceData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HmcDataUtilsTest {

    @Test
    void getLatestPartiesNotifiedResponse_WhenEmptyList_ReturnsNull() {
        PartiesNotifiedResponses partiesNotified = PartiesNotifiedResponses.builder().responses(List.of()).build();

        PartiesNotifiedResponse result = HmcDataUtils.getLatestHearingNoticeDetails(partiesNotified);

        assertNull(result);
    }

    @Test
    void getLatestPartiesNotifiedResponse_WhenNonEmptyList_ReturnsLatestResponse() {
        LocalDateTime now = LocalDateTime.now();

        var res1 = PartiesNotifiedResponse.builder().serviceData(PartiesNotifiedServiceData.builder().hearingLocation("loc-3").build())
            .responseReceivedDateTime(now.minusDays(3)).build();
        var res2 = PartiesNotifiedResponse.builder().serviceData(PartiesNotifiedServiceData.builder().hearingLocation("loc-2").build())
            .responseReceivedDateTime(now.minusDays(2)).build();
        var expected = PartiesNotifiedResponse.builder().serviceData(PartiesNotifiedServiceData.builder().hearingLocation("loc-1").build())
            .responseReceivedDateTime(now.minusDays(1)).build();

        PartiesNotifiedResponses partiesNotified = PartiesNotifiedResponses.builder()
            .responses(List.of(res1, expected, res2))
            .build();

        PartiesNotifiedResponse result = HmcDataUtils.getLatestHearingNoticeDetails(partiesNotified);

        assertEquals(result, expected);
    }

    @Test
    void hearingDataChanged_WhenHearingDataChanged_ReturnsTrue() {
        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(
                HearingResponse.builder().hearingDaySchedule(
                        List.of(
                            HearingDaySchedule.builder()
                                .hearingVenueId("Venue A")
                                .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
                                .build()))
                    .build())
            .build();

        PartiesNotifiedResponse partiesNotified = PartiesNotifiedResponse.builder()
            .serviceData(PartiesNotifiedServiceData.builder()
                             .hearingDate(LocalDateTime.of(2023, 5, 23, 10, 0))
                             .hearingLocation("Venue B")
                             .build()).build();

        boolean result = HmcDataUtils.hearingDataChanged(partiesNotified, hearing);

        assertTrue(result);
    }

    @Test
    void hearingDataChanged_WhenHearingDataNotChanged_ReturnsFalse() {
        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(
                HearingResponse.builder().hearingDaySchedule(
                        List.of(
                            HearingDaySchedule.builder()
                                .hearingVenueId("Venue A")
                                .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
                                .build()))
                    .build())
            .build();

        PartiesNotifiedResponse partiesNotified = PartiesNotifiedResponse.builder()
            .serviceData(PartiesNotifiedServiceData.builder()
                             .hearingDate(LocalDateTime.of(2023, 5, 23, 10, 0))
                             .hearingLocation("Venue A")
                             .build()).build();

        boolean result = HmcDataUtils.hearingDataChanged(partiesNotified, hearing);

        assertFalse(result);
    }

    @Test
    void hearingDataChanged_WhenPartiesNotifiedIsNull_ReturnsTrue() {
        HearingGetResponse hearing = hearingResponse().build();

        boolean result = HmcDataUtils.hearingDataChanged(null, hearing);

        assertTrue(result);
    }

    @Test
    void hearingDataChanged_WhenPartiesNotifiedServiceDataIsNull_ReturnsTrue() {
        HearingGetResponse hearing = hearingResponse().build();
        PartiesNotifiedResponse partiesNotified = PartiesNotifiedResponse.builder().build();

        boolean result = HmcDataUtils.hearingDataChanged(partiesNotified, hearing);

        assertTrue(result);
    }

    private HearingGetResponse.HearingGetResponseBuilder hearingResponse() {
        return HearingGetResponse.builder()
            .requestDetails(HearingRequestDetails.builder().build())
            .hearingDetails(HearingDetails.builder().build())
            .caseDetails(CaseDetailsHearing.builder().build())
            .hearingResponse(HearingResponse.builder().build())
            .partyDetails(List.of());
    }

    @Test
    void getHearingDaysText() {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 13, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 24, 15, 0))
            .build();

        var hearingDay3 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 25, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 25, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysText(hearing);

        assertEquals(result, "23 May 2023 at 10:00 for 3 hours\n" +
            "24 May 2023 at 10:00 for 5 hours\n" +
            "25 May 2023 at 10:00 for 5 hours");
    }

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

    @Test
    void getHearingDaysText_shouldReturnExpectedTextForASingle6HourDay() {
        var hearingDay = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(List.of(hearingDay)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing);

        assertEquals(result, List.of("23 May 2023 at 10:00 for 5 hours"));
    }

    @Test
    void getHearingDaysText_shouldReturnExpectedTextForASingle5HourDay() {
        var hearingDay = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 15, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(List.of(hearingDay)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing);

        assertEquals(result, List.of("23 May 2023 at 10:00 for 5 hours"));
    }

    @Test
    void getHearingDaysText_shouldReturnExpectedTextForASingle3HourDay() {
        var hearingDay = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 13, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(List.of(hearingDay)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing);

        assertEquals(result, List.of("23 May 2023 at 10:00 for 3 hours"));
    }

    @Test
    void getHearingDaysText_shouldReturnExpectedTextForASingleHourDay() {
        var hearingDay = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 11, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(List.of(hearingDay)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing);

        assertEquals(result, List.of("23 May 2023 at 10:00 for 1 hour"));
    }

    @Test
    void getHearingDaysText_shouldReturnExpectedTextForMultipleDays() {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 13, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 24, 15, 0))
            .build();

        var hearingDay3 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 25, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 25, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing);

        assertEquals(result, List.of(
            "23 May 2023 at 10:00 for 3 hours",
            "24 May 2023 at 10:00 for 5 hours",
            "25 May 2023 at 10:00 for 5 hours"));
    }

    @Test
    void getTotalHearingDurationText_whenTearingDurationGreaterThan6Hours() {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 13, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 24, 15, 0))
            .build();

        var hearingDay3 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 25, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 25, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing);

        assertEquals(result, "2 days and 2 hours");
    }

    @Test
    void getTotalHearingDurationText_whenHearingDurationContainsSingleDay() {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 13, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 24, 15, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing);

        assertEquals(result, "1 day and 2 hours");
    }

    @Test
    void getTotalHearingDurationText_whenTearingDurationLessThan6Hours() {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 12, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 24, 12, 0))
            .build();

        var hearingDay3 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 25, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 25, 11, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing);

        assertEquals(result, "5 hours");
    }

    @Test
    void getTotalHearingDurationText_whenTearingDurationOf6Hours() {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 12, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 24, 12, 0))
            .build();

        var hearingDay3 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 25, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 25, 12, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing);

        assertEquals(result, "1 day");
    }
}
