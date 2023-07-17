package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.hmc.model.hearing.CaseDetailsHearing;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingRequestDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;
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
                                .build(),
                            HearingDaySchedule.builder()
                                .hearingVenueId("Venue A")
                                .hearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0))
                                .build()))
                    .build())
            .build();

        PartiesNotifiedResponse partiesNotified = PartiesNotifiedResponse.builder()
            .serviceData(PartiesNotifiedServiceData.builder()
                             .days(List.of(
                                 HearingDay.builder()
                                               .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
                                               .build(),
                                 HearingDay.builder()
                                               .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
                                               .build()))
                             .hearingLocation("Venue A")
                             .build()).build();

        boolean result = HmcDataUtils.hearingDataChanged(partiesNotified, hearing);

        assertTrue(result);
    }

    @Test
    void hearingDataChanged_WhenHearingDataChanged_ReturnsTrueExtraDay() {
        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(
                HearingResponse.builder().hearingDaySchedule(
                        List.of(
                            HearingDaySchedule.builder()
                                .hearingVenueId("Venue A")
                                .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
                                .build(),
                            HearingDaySchedule.builder()
                                .hearingVenueId("Venue A")
                                .hearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0))
                                .build()))
                    .build())
            .build();

        PartiesNotifiedResponse partiesNotified = PartiesNotifiedResponse.builder()
            .serviceData(PartiesNotifiedServiceData.builder()
                             .days(List.of(HearingDay.builder()
                                               .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
                                               .build()))
                             .hearingLocation("Venue A")
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
                             .days(List.of(HearingDay.builder()
                                               .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
                                               .build()))
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
