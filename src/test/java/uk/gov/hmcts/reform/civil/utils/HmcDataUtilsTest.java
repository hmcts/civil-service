package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.HearingIndividual;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.hmc.model.hearing.Attendees;
import uk.gov.hmcts.reform.hmc.model.hearing.CaseDetailsHearing;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingRequestDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.OrganisationDetailsModel;
import uk.gov.hmcts.reform.hmc.model.hearing.PartyDetailsModel;
import uk.gov.hmcts.reform.hmc.model.hearings.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.hearings.HearingsResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedServiceData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.includesVideoHearing;
import static uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel.INTER;
import static uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel.VIDCVP;

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
                                .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
                                .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 11, 0))
                                .build(),
                            HearingDaySchedule.builder()
                                .hearingVenueId("Venue A")
                                .hearingStartDateTime(LocalDateTime.of(2023, 12, 24, 10, 0))
                                .hearingEndDateTime(LocalDateTime.of(2023, 12, 24, 11, 0))
                                .build()))
                    .build())
            .build();

        PartiesNotifiedResponse partiesNotified = PartiesNotifiedResponse.builder()
            .serviceData(PartiesNotifiedServiceData.builder()
                             .days(List.of(
                                 HearingDay.builder()
                                               .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
                                               .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 11, 0))
                                               .build(),
                                 HearingDay.builder()
                                               .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
                                               .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 11, 0))
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
                             .hearingDate(LocalDateTime.of(2023, 12, 23, 10, 0))
                             .hearingLocation("Venue B")
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
                                .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
                                .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 11, 0))
                                .build()))
                    .build())
            .build();

        PartiesNotifiedResponse partiesNotified = PartiesNotifiedResponse.builder()
            .serviceData(PartiesNotifiedServiceData.builder()
                             .days(List.of(HearingDay.builder()
                                               .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
                                               .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 11, 0))
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 13, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 24, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 24, 16, 0))
            .build();

        var hearingDay3 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 25, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 25, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "23 Rhagfyr 2023 am 10:00 am 3 oriau\n" +
            "24 Rhagfyr 2023 am 14:00 am 2 oriau\n" +
            "25 Rhagfyr 2023 am 10:00 am 5 oriau"
            : "23 December 2023 at 10:00 for 3 hours\n" +
            "24 December 2023 at 14:00 for 2 hours\n" +
            "25 December 2023 at 10:00 for 5 hours");
    }

    @Test
    void getTitle() {
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_1Day_FullDay_BstHearingDay(Boolean isWelsh) {
        var hearingDay = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(List.of(hearingDay)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        assertEquals(result, List.of(isWelsh ? "23 Mai 2023 am 11:00 am 5 oriau" : "23 May 2023 at 11:00 for 5 hours"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_1Day_FullDay(Boolean isWelsh) {
        var hearingDay = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(List.of(hearingDay)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        assertEquals(result, List.of(isWelsh ? "23 Rhagfyr 2023 am 10:00 am 5 oriau" : "23 December 2023 at 10:00 for 5 hours"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_1Day_Morning(Boolean isWelsh) {
        var hearingDay = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 13, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(List.of(hearingDay)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        assertEquals(result, List.of(isWelsh ? "23 Rhagfyr 2023 am 10:00 am 3 oriau" : "23 December 2023 at 10:00 for 3 hours"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_1Day_Afternoon(Boolean isWelsh) {
        var hearingDay = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(List.of(hearingDay)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        assertEquals(result, List.of(isWelsh ? "23 Rhagfyr 2023 am 14:00 am 2 oriau" : "23 December 2023 at 14:00 for 2 hours"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_2Days_MorningAndAfternoon_BST(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 13, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 24, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 24, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedList = isWelsh
            ? List.of(
            "23 Mai 2023 am 11:00 am 3 oriau",
            "24 Mai 2023 am 15:00 am 2 oriau")
            : List.of(
            "23 May 2023 at 11:00 for 3 hours",
            "24 May 2023 at 15:00 for 2 hours");

        assertEquals(result, expectedList);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_2Days_MorningAndAfternoon(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 13, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 24, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 24, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedList = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 10:00 am 3 oriau",
            "24 Rhagfyr 2023 am 14:00 am 2 oriau")
            : List.of(
            "23 December 2023 at 10:00 for 3 hours",
            "24 December 2023 at 14:00 for 2 hours");

        assertEquals(result, expectedList);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_2Days_FullDayAndAfternoon(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 24, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 24, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 10:00 am 5 oriau",
            "24 Rhagfyr 2023 am 14:00 am 2 oriau")
            : List.of(
            "23 December 2023 at 10:00 for 5 hours",
            "24 December 2023 at 14:00 for 2 hours");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_2Days_FullDayAndMorning(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 24, 13, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 10:00 am 5 oriau",
            "24 Rhagfyr 2023 am 10:00 am 3 oriau")
            : List.of(
            "23 December 2023 at 10:00 for 5 hours",
            "24 December 2023 at 10:00 for 3 hours");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_2Days_Morning(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 13, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 24, 13, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 10:00 am 3 oriau",
            "24 Rhagfyr 2023 am 10:00 am 3 oriau")
            : List.of(
            "23 December 2023 at 10:00 for 3 hours",
            "24 December 2023 at 10:00 for 3 hours");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_2Days_Afternoon(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 24, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 24, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 14:00 am 2 oriau",
            "24 Rhagfyr 2023 am 14:00 am 2 oriau")
            : List.of(
            "23 December 2023 at 14:00 for 2 hours",
            "24 December 2023 at 14:00 for 2 hours");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_3Days_FullDays(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 24, 16, 0))
            .build();

        var hearingDay3 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 25, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 25, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 10:00 am 5 oriau",
            "24 Rhagfyr 2023 am 10:00 am 5 oriau",
            "25 Rhagfyr 2023 am 10:00 am 5 oriau")
            : List.of(
            "23 December 2023 at 10:00 for 5 hours",
            "24 December 2023 at 10:00 for 5 hours",
            "25 December 2023 at 10:00 for 5 hours");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_3Days_FullDayMorningAndAfternoon(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 24, 13, 0))
            .build();

        var hearingDay3 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 25, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 25, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 10:00 am 5 oriau",
            "24 Rhagfyr 2023 am 10:00 am 3 oriau",
            "25 Rhagfyr 2023 am 14:00 am 2 oriau")
            : List.of(
            "23 December 2023 at 10:00 for 5 hours",
            "24 December 2023 at 10:00 for 3 hours",
            "25 December 2023 at 14:00 for 2 hours");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_3Days_Afternoon(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 24, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 24, 16, 0))
            .build();

        var hearingDay3 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 25, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 25, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 14:00 am 2 oriau",
            "24 Rhagfyr 2023 am 14:00 am 2 oriau",
            "25 Rhagfyr 2023 am 14:00 am 2 oriau")
            : List.of(
            "23 December 2023 at 14:00 for 2 hours",
            "24 December 2023 at 14:00 for 2 hours",
            "25 December 2023 at 14:00 for 2 hours");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_1Hour30minutes(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 14, 30))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ?  List.of("23 Rhagfyr 2023 am 14:30 am 1 awr a 30 munudau")
            : List.of("23 December 2023 at 14:30 for 1 hour and 30 minutes");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_30minutes(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 14, 30))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedList = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 14:00 am 30 munudau")
            : List.of(
            "23 December 2023 at 14:00 for 30 minutes");
        assertEquals(result, expectedList);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_1Hour15minutes(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 14, 45))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 14:45 am 1 awr a 15 munudau")
            : List.of(
            "23 December 2023 at 14:45 for 1 hour and 15 minutes");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_1Hour45minutes(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 12, 23, 14, 15))
            .hearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1)).build())
            .build();

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 14:15 am 1 awr a 45 munudau")
            : List.of(
            "23 December 2023 at 14:15 for 1 hour and 45 minutes");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_1Day_3Hours_30minutes(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 10, 23, 10, 00))
            .hearingEndDateTime(LocalDateTime.of(2023, 10, 23, 16, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 10, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 10, 24, 13, 30))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 dydd a 3 oriau a 30 munudau" : "1 day and 3 hours and 30 minutes");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_1Day_1Hours_30minutes(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 10, 23, 10, 00))
            .hearingEndDateTime(LocalDateTime.of(2023, 10, 23, 14, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 10, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 10, 24, 13, 30))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 dydd ac 1 awr a 30 munudau" : "1 day and 1 hour and 30 minutes");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_whenDurationLessThan1Hour(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 10, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 10, 23, 10, 30))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "30 munudau" : "30 minutes");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_whenDuration5Hours(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 10, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 10, 23, 15, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "5 oriau" : "5 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_whenDuration5Hours30Minutes(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 10, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 10, 23, 15, 30))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "5 oriau a 30 munudau" : "5 hours and 30 minutes");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_whenDuration1Day5Hours(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 10, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 10, 23, 15, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "5 oriau" : "5 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_whenDuration1day30Minutes(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 10, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 10, 23, 16, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 10, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 10, 24, 10, 30))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 dydd a 30 munudau" : "1 day and 30 minutes");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_whenDuration1Day5Hours30Minutes(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 10, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 10, 23, 16, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 10, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 10, 24, 15, 30))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 dydd a 5 oriau a 30 munudau" : "1 day and 5 hours and 30 minutes");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_1Day_FullDay(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 dydd" : "1 day");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_1Day_Morning(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 13, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "3 oriau" : "3 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_1Day_Afternoon(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "2 oriau" : "2 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_2Days_MorningAndAfternoon(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 13, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 24, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 24, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "5 oriau" : "5 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_2Days_FullDayAndAfternoon(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 24, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 24, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 dydd a 2 oriau" : "1 day and 2 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_2Days_FullDayAndMorning(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 24, 13, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 dydd a 3 oriau" : "1 day and 3 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_2Days_Morning(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 13, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 24, 13, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 dydd" : "1 day");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_2Days_Afternoon(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 24, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 24, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "4 oriau" : "4 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_3Days_FullDays(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 24, 16, 0))
            .build();

        var hearingDay3 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 25, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 25, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "3 dyddiau" : "3 days");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_3Days_FullDayMorningAndAfternoon(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 24, 13, 0))
            .build();

        var hearingDay3 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 25, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 25, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 dydd a 5 oriau" : "1 day and 5 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_3Days_Afternoon(Boolean isWelsh) {
        var hearingDay1 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 23, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0))
            .build();

        var hearingDay2 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 24, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 24, 16, 0))
            .build();

        var hearingDay3 = HearingDaySchedule.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 5, 25, 14, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 5, 25, 16, 0))
            .build();

        HearingGetResponse hearing = hearingResponse()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)).build())
            .build();

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 dydd" : "1 day");
    }

    @Nested
    class IncludesVideoHearing {
        @Test
        void shouldReturnFalseIfCaseHearingsIsNull() {
            HearingsResponse hearings = HearingsResponse.builder().build();

            boolean actual = includesVideoHearing(hearings);

            assertFalse(actual);
        }

        @Test
        void shouldReturnFalseIfNoCaseHearingsExist() {
            HearingsResponse hearings = HearingsResponse.builder().caseHearings(List.of()).build();

            boolean actual = includesVideoHearing(hearings);

            assertFalse(actual);
        }

        @Test
        void shouldReturnFalseIfNoVideoHearingsExist() {
            HearingsResponse hearings = HearingsResponse.builder().caseHearings(List.of(
                CaseHearing.builder()
                    .hearingDaySchedule(List.of(
                        HearingDaySchedule.builder()
                            .attendees(List.of(
                                Attendees.builder()
                                    .hearingSubChannel(INTER)
                                    .build(),
                                Attendees.builder()
                                    .hearingSubChannel(null)
                                    .build()
                            )).build()))
                    .build()
            )).build();

            boolean actual = includesVideoHearing(hearings);

            assertFalse(actual);
        }

        @Test
        void shouldReturnTrue_IfVideoHearingsExistOnASingleDay() {
            HearingsResponse hearings = HearingsResponse.builder().caseHearings(List.of(
                CaseHearing.builder()
                    .hearingDaySchedule(List.of(
                        HearingDaySchedule.builder()
                            .attendees(List.of(
                                Attendees.builder()
                                    .hearingSubChannel(VIDCVP)
                                    .build(),
                                Attendees.builder()
                                    .hearingSubChannel(null)
                                    .build()
                            )).build()))
                    .build()
            )).build();

            boolean actual = includesVideoHearing(hearings);

            assertTrue(actual);
        }

        @Test
        void shouldReturnTrue_IfVideoHearingsExistOneDayWithinMultipleDays() {
            HearingsResponse hearings = HearingsResponse.builder().caseHearings(List.of(
                CaseHearing.builder()
                    .hearingDaySchedule(List.of(
                        HearingDaySchedule.builder()
                            .attendees(List.of(
                                Attendees.builder()
                                    .hearingSubChannel(INTER)
                                    .build(),
                                Attendees.builder()
                                    .hearingSubChannel(null)
                                    .build()
                            )).build(),
                        HearingDaySchedule.builder()
                            .attendees(List.of(
                                Attendees.builder()
                                    .hearingSubChannel(VIDCVP)
                                    .build(),
                                Attendees.builder()
                                    .hearingSubChannel(null)
                                    .build()
                            )).build()
                    ))
                    .build()
            )).build();

            boolean actual = includesVideoHearing(hearings);

            assertTrue(actual);
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    class GetHearingLocation {

        @MockBean
        private LocationReferenceDataService locationRefDataService;

        @Test
        void shouldReturnLocation_whenInvoked() {
            List<LocationRefData> locations = List.of(LocationRefData.builder().epimmsId("venue").build());
            when(locationRefDataService.getHearingCourtLocations("authToken"))
                .thenReturn(locations);
            LocationRefData locationRefData = HmcDataUtils.getLocationRefData(
                "HER123",
                "venue",
                "authToken",
                locationRefDataService
            );

            assertThat(locationRefData).isEqualTo(LocationRefData.builder().epimmsId("venue").build());
        }

        @Test
        void shouldThrowException_whenLocationIsNull() {
            when(locationRefDataService.getHearingCourtLocations("abc"))
                .thenReturn(null);
            assertThrows(
                IllegalArgumentException.class,
                () -> HmcDataUtils.getLocationRefData(
                    "HER123",
                    "abc",
                    "authToken",
                    locationRefDataService));
        }
    }

    @Nested
    class GetLatestHearing {
        private static final LocalDateTime TODAY = LocalDateTime.of(2024, 1, 22, 0, 0, 0);

        @Test
        void shouldGetLatestHearing() {
            LocalDateTime hearingStartTime = TODAY.plusHours(10);
            LocalDateTime requestedDateTime = TODAY.plusHours(9);
            String hearingId = "12345";

            HearingsResponse hearingsResponse = HearingsResponse.builder()
                .caseHearings(List.of(
                    hearing("11111", TODAY.minusDays(1), List.of(hearingStartTime)),
                    hearing(hearingId, requestedDateTime, List.of(hearingStartTime)),
                    hearing("33333", TODAY.minusDays(3), List.of(hearingStartTime)),
                    hearing("22222", TODAY.minusDays(2), List.of(hearingStartTime))))
                .build();

            CaseHearing actual = HmcDataUtils.getLatestHearing(hearingsResponse);
            CaseHearing expected = hearing(hearingId, requestedDateTime, List.of(hearingStartTime));
            assertEquals(expected, actual);
        }
    }

    @Nested
    class GetHearingTypeTitleText {

        @ParameterizedTest
        @CsvSource({
            "TRI, SMALL_CLAIM, hearing",
            "TRI, FAST_CLAIM, trial",
            "DRH, SMALL_CLAIM, dispute resolution hearing",
            "DIS, SMALL_CLAIM, disposal hearing",
        })
        void shouldReturnExpectedTitle(String hearingType, AllocatedTrack allocatedTrack, String expected) {
            HearingGetResponse hearing = buildHearing(hearingType);
            CaseData caseData = CaseData.builder().allocatedTrack(allocatedTrack).build();

            String actual = HmcDataUtils.getHearingTypeTitleText(caseData, hearing);

            assertEquals(expected, actual);
        }

        @ParameterizedTest
        @CsvSource({
            "TRI, SMALL_CLAIM, hearing",
            "TRI, FAST_CLAIM, trial",
            "DRH, SMALL_CLAIM, dispute resolution hearing",
            "DIS, SMALL_CLAIM, disposal hearing",
        })
        void shouldReturnExpectedTitle_specClaim(String hearingType, AllocatedTrack allocatedTrack, String expected) {
            HearingGetResponse hearing = buildHearing(hearingType);
            CaseData caseData = CaseData.builder().responseClaimTrack(allocatedTrack.name()).build();

            String actual = HmcDataUtils.getHearingTypeTitleText(caseData, hearing);

            assertEquals(expected, actual);
        }
    }

    @Nested
    class GetHearingTypeContentText {

        @ParameterizedTest
        @CsvSource({
            "TRI, SMALL_CLAIM, hearing",
            "TRI, FAST_CLAIM, trial",
            "DRH, SMALL_CLAIM, hearing",
            "DIS, SMALL_CLAIM, hearing",
        })
        void shouldReturnExpectedText_unspecClaim(String hearingType, AllocatedTrack allocatedTrack, String expected) {
            HearingGetResponse hearing = buildHearing(hearingType);
            CaseData caseData = CaseData.builder().allocatedTrack(allocatedTrack).build();

            String actual = HmcDataUtils.getHearingTypeContentText(caseData, hearing);

            assertEquals(expected, actual);
        }

        @ParameterizedTest
        @CsvSource({
            "TRI, SMALL_CLAIM, hearing",
            "TRI, FAST_CLAIM, trial",
            "DRH, SMALL_CLAIM, hearing",
            "DIS, SMALL_CLAIM, hearing",
        })
        void shouldReturnExpectedText_specClaim(String hearingType, AllocatedTrack allocatedTrack, String expected) {
            HearingGetResponse hearing = buildHearing(hearingType);
            CaseData caseData = CaseData.builder().responseClaimTrack(allocatedTrack.name()).build();

            String actual = HmcDataUtils.getHearingTypeContentText(caseData, hearing);

            assertEquals(expected, actual);
        }
    }

    @Nested
    class GetInPersonAttendees {

        @Test
        void shouldReturnOneAttendee() {
            HearingGetResponse hearing = buildHearing(
                    List.of(HearingIndividual.attendingHearingInPerson("Jason", "Wells"),
                            HearingIndividual.attendingHearingByPhone("Chloe", "Landale"),
                            HearingIndividual.attendingHearingByVideo("Jenny", "Harper")));

            String actual = HmcDataUtils.getInPersonAttendeeNames(hearing);

            assertEquals("Jason Wells", actual);
        }

        @Test
        void shouldReturnMultipleAttendees() {
            HearingGetResponse hearing = buildHearing(
                    List.of(HearingIndividual.attendingHearingInPerson("Jason", "Wells"),
                            HearingIndividual.attendingHearingByPhone("Chloe", "Landale"),
                            HearingIndividual.attendingHearingInPerson("Michael", "Carver"),
                            HearingIndividual.attendingHearingByVideo("Jenny", "Harper"),
                            HearingIndividual.attendingHearingInPerson("Jack", "Crawley")
                    ));

            String actual = HmcDataUtils.getInPersonAttendeeNames(hearing);

            assertEquals("Jason Wells\nMichael Carver\nJack Crawley", actual);
        }

        @Test
        void shouldReturnNullForZeroAttendees() {
            HearingGetResponse hearing = buildHearing(
                    List.of(HearingIndividual.attendingHearingByPhone("Chloe", "Landale"),
                            HearingIndividual.attendingHearingByVideo("Jenny", "Harper")
                    ));

            String actual = HmcDataUtils.getInPersonAttendeeNames(hearing);

            assertNull(actual);
        }

        @Test
        void shouldNotThrowNpeWhenAttendeesContainsOrgNotIndividualFromListAssistMisuse() {
            HearingGetResponse hearing = buildHearingWithOrganisation(
                List.of(HearingIndividual.attendingHearingInPerson("Jason", "Wells"),
                        HearingIndividual.attendingHearingByPhone("Chloe", "Landale"),
                        HearingIndividual.attendingHearingInPerson("Michael", "Carver"),
                        HearingIndividual.attendingHearingByVideo("Jenny", "Harper"),
                        HearingIndividual.attendingHearingInPerson("Jack", "Crawley")
                ),
                PartyDetailsModel.builder().hearingSubChannel(INTER.name()).partyID("PARTYID")
                    .organisationDetails(OrganisationDetailsModel.builder()
                                             .cftOrganisationID("ID")
                                             .name("Misplaced Org")
                                             .build()).build()
                );

            String actual = HmcDataUtils.getInPersonAttendeeNames(hearing);

            assertEquals("Jason Wells\nMichael Carver\nJack Crawley", actual);
        }

    }

    @Nested
    class GetPhoneAttendees {

        @Test
        void shouldReturnOneAttendee() {
            HearingGetResponse hearing = buildHearing(
                    List.of(HearingIndividual.attendingHearingByPhone("Jason", "Wells"),
                            HearingIndividual.attendingHearingInPerson("Chloe", "Landale"),
                            HearingIndividual.attendingHearingByVideo("Jenny", "Harper")));

            String actual = HmcDataUtils.getPhoneAttendeeNames(hearing);

            assertEquals("Jason Wells", actual);
        }

        @Test
        void shouldReturnMultipleAttendees() {
            HearingGetResponse hearing = buildHearing(
                    List.of(HearingIndividual.attendingHearingByPhone("Jason", "Wells"),
                            HearingIndividual.attendingHearingInPerson("Chloe", "Landale"),
                            HearingIndividual.attendingHearingByPhone("Michael", "Carver"),
                            HearingIndividual.attendingHearingByVideo("Jenny", "Harper"),
                            HearingIndividual.attendingHearingByPhone("Jack", "Crawley")
                    ));

            String actual = HmcDataUtils.getPhoneAttendeeNames(hearing);

            assertEquals("Jason Wells\nMichael Carver\nJack Crawley", actual);
        }

        @Test
        void shouldReturnNull() {
            HearingGetResponse hearing = buildHearing(
                    List.of(HearingIndividual.attendingHearingInPerson("Chloe", "Landale"),
                            HearingIndividual.attendingHearingByVideo("Jenny", "Harper")
                    ));

            String actual = HmcDataUtils.getPhoneAttendeeNames(hearing);

            assertNull(actual);
        }

    }

    @Nested
    class GetVideoAttendees {

        @Test
        void shouldReturnOneAttendee() {
            HearingGetResponse hearing = buildHearing(
                    List.of(HearingIndividual.attendingHearingByVideo("Jason", "Wells"),
                            HearingIndividual.attendingHearingInPerson("Chloe", "Landale"),
                            HearingIndividual.attendingHearingByPhone("Jenny", "Harper")));

            String actual = HmcDataUtils.getVideoAttendeesNames(hearing);

            assertEquals("Jason Wells", actual);
        }

        @Test
        void shouldReturnMultipleAttendees() {
            HearingGetResponse hearing = buildHearing(
                    List.of(HearingIndividual.attendingHearingByVideo("Jason", "Wells"),
                            HearingIndividual.attendingHearingInPerson("Chloe", "Landale"),
                            HearingIndividual.attendingHearingByVideo("Michael", "Carver"),
                            HearingIndividual.attendingHearingByPhone("Jenny", "Harper"),
                            HearingIndividual.attendingHearingByVideo("Jack", "Crawley")
                    ));

            String actual = HmcDataUtils.getVideoAttendeesNames(hearing);

            assertEquals("Jason Wells\nMichael Carver\nJack Crawley", actual);
        }

        @Test
        void shouldReturnNullForZeroAttendees() {
            HearingGetResponse hearing = buildHearing(
                    List.of(HearingIndividual.attendingHearingInPerson("Chloe", "Landale"),
                            HearingIndividual.attendingHearingByPhone("Jenny", "Harper")
                    ));

            String actual = HmcDataUtils.getVideoAttendeesNames(hearing);

            assertNull(actual);
        }

    }

    private HearingGetResponse buildHearing(List<HearingIndividual> testIndividuals) {
        return HearingGetResponse.builder()
                .partyDetails(testIndividuals.stream().map(HearingIndividual::buildPartyDetails).toList())
                .hearingResponse(HearingResponse.builder().hearingDaySchedule(List.of(
                        HearingDaySchedule.builder()
                                .attendees(testIndividuals.stream().map(HearingIndividual::buildAttendee).toList())
                                .build())).build())
                .build();
    }

    private HearingGetResponse buildHearing(String hearingType) {
        return HearingGetResponse.builder()
            .hearingDetails(HearingDetails.builder().hearingType(hearingType).build())
            .build();
    }

    private HearingGetResponse buildHearingWithOrganisation(List<HearingIndividual> testIndividuals,
                                                            PartyDetailsModel org) {
        List<PartyDetailsModel> partyDetails = new ArrayList<>(testIndividuals.stream()
                                                                   .map(HearingIndividual::buildPartyDetails).toList());

        partyDetails.add(org);

        return HearingGetResponse.builder()
            .partyDetails(partyDetails)
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(List.of(
                HearingDaySchedule.builder()
                    .attendees(testIndividuals.stream().map(HearingIndividual::buildAttendee).toList())
                    .build())).build())
            .build();
    }

    private CaseHearing hearing(String hearingId, LocalDateTime hearingRequestTime, List<LocalDateTime> startTimes) {
        return CaseHearing.builder()
            .hearingId(Long.valueOf(hearingId))
            .hearingRequestDateTime(hearingRequestTime)
            .hearingDaySchedule(startTimes.stream().map(startTime -> HearingDaySchedule.builder().hearingStartDateTime(
                startTime).build()).collect(
                Collectors.toList()))
            .build();
    }

    @Nested
    class IsClaimantDQDocumentsWelshTests {

        @Test
        void shouldReturnFalse_whenApplicant1DQIsNull() {
            // Given
            CaseData caseData = CaseData.builder()
                .applicant1DQ(null)
                .build();

            // When
            boolean result = HmcDataUtils.isClaimantDQDocumentsWelsh(caseData);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalse_whenApplicant1DQLanguageIsNull() {
            // Given
            Applicant1DQ applicant1DQ = Applicant1DQ.builder()
                .applicant1DQLanguage(null)
                .build();
            CaseData caseData = CaseData.builder()
                .applicant1DQ(applicant1DQ)
                .build();

            // When
            boolean result = HmcDataUtils.isClaimantDQDocumentsWelsh(caseData);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalse_whenDocumentsIsEnglish() {
            // Given
            WelshLanguageRequirements req = WelshLanguageRequirements.builder()
                .documents(Language.ENGLISH)
                .build();
            Applicant1DQ applicant1DQ = Applicant1DQ.builder()
                .applicant1DQLanguage(req)
                .build();
            CaseData caseData = CaseData.builder()
                .applicant1DQ(applicant1DQ)
                .build();

            // When
            boolean result = HmcDataUtils.isClaimantDQDocumentsWelsh(caseData);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnTrue_whenDocumentsIsWelsh() {
            // Given
            WelshLanguageRequirements req = WelshLanguageRequirements.builder()
                .documents(Language.WELSH)
                .build();
            Applicant1DQ applicant1DQ = Applicant1DQ.builder()
                .applicant1DQLanguage(req)
                .build();
            CaseData caseData = CaseData.builder()
                .applicant1DQ(applicant1DQ)
                .build();

            // When
            boolean result = HmcDataUtils.isClaimantDQDocumentsWelsh(caseData);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnTrue_whenDocumentsIsBoth() {
            // Given
            WelshLanguageRequirements req = WelshLanguageRequirements.builder()
                .documents(Language.BOTH)
                .build();
            Applicant1DQ applicant1DQ = Applicant1DQ.builder()
                .applicant1DQLanguage(req)
                .build();
            CaseData caseData = CaseData.builder()
                .applicant1DQ(applicant1DQ)
                .build();

            // When
            boolean result = HmcDataUtils.isClaimantDQDocumentsWelsh(caseData);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    class IsDefendantDQDocumentsWelshTests {

        @Test
        void shouldReturnFalse_whenRespondent1DQIsNull() {
            // Given
            CaseData caseData = CaseData.builder()
                .respondent1DQ(null)
                .build();

            // When
            boolean result = HmcDataUtils.isDefendantDQDocumentsWelsh(caseData);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalse_whenRespondent1DQLanguageIsNull() {
            // Given
            Respondent1DQ respondent1DQ = Respondent1DQ.builder()
                .respondent1DQLanguage(null)
                .build();
            CaseData caseData = CaseData.builder()
                .respondent1DQ(respondent1DQ)
                .build();

            // When
            boolean result = HmcDataUtils.isDefendantDQDocumentsWelsh(caseData);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalse_whenDocumentsIsEnglish() {
            // Given
            WelshLanguageRequirements req = WelshLanguageRequirements.builder()
                .documents(Language.ENGLISH)
                .build();
            Respondent1DQ respondent1DQ = Respondent1DQ.builder()
                .respondent1DQLanguage(req)
                .build();
            CaseData caseData = CaseData.builder()
                .respondent1DQ(respondent1DQ)
                .build();

            // When
            boolean result = HmcDataUtils.isDefendantDQDocumentsWelsh(caseData);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnTrue_whenDocumentsIsWelsh() {
            // Given
            WelshLanguageRequirements req = WelshLanguageRequirements.builder()
                .documents(Language.WELSH)
                .build();
            Respondent1DQ respondent1DQ = Respondent1DQ.builder()
                .respondent1DQLanguage(req)
                .build();
            CaseData caseData = CaseData.builder()
                .respondent1DQ(respondent1DQ)
                .build();

            // When
            boolean result = HmcDataUtils.isDefendantDQDocumentsWelsh(caseData);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnTrue_whenDocumentsIsBoth() {
            // Given
            WelshLanguageRequirements req = WelshLanguageRequirements.builder()
                .documents(Language.BOTH)
                .build();
            Respondent1DQ respondent1DQ = Respondent1DQ.builder()
                .respondent1DQLanguage(req)
                .build();
            CaseData caseData = CaseData.builder()
                .respondent1DQ(respondent1DQ)
                .build();

            // When
            boolean result = HmcDataUtils.isDefendantDQDocumentsWelsh(caseData);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    class IsWelshHearingTemplateTests {

        @Test
        void shouldReturnTrue_whenApplicantNoRepAndClaimantBilingual() {
            // Given
            CaseData caseData = CaseData.builder()
                .applicant1Represented(YesOrNo.NO)
                // -> isClaimantBilingual() = true
                .claimantBilingualLanguagePreference(Language.WELSH.toString())
                .respondent1Represented(YesOrNo.YES)
                .build();

            // When
            boolean result = HmcDataUtils.isWelshHearingTemplate(caseData);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnTrue_whenApplicantNoRepAndClaimantDQDocumentsWelsh() {
            // Given
            WelshLanguageRequirements req = WelshLanguageRequirements.builder()
                .documents(Language.BOTH)
                .build();
            Applicant1DQ dq = Applicant1DQ.builder()
                .applicant1DQLanguage(req)
                .build();

            CaseData caseData = CaseData.builder()
                .applicant1Represented(YesOrNo.NO)
                // -> isClaimantBilingual() = false (ej: ENGLISH)
                .claimantBilingualLanguagePreference(Language.ENGLISH.toString())
                .applicant1DQ(dq)
                .respondent1Represented(YesOrNo.YES)
                .build();

            // When
            boolean result = HmcDataUtils.isWelshHearingTemplate(caseData);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnTrue_whenRespondentNoRepAndRespondentResponseBilingual() {
            // Given
            // -> isRespondentResponseBilingual() = true si "BOTH" o "WELSH"
            CaseDataLiP caseDataLiP = CaseDataLiP.builder()
                .respondent1LiPResponse(RespondentLiPResponse.builder()
                                            .respondent1ResponseLanguage("BOTH") // => true
                                            .build())
                .build();

            CaseData caseData = CaseData.builder()
                .applicant1Represented(YesOrNo.YES)
                .respondent1Represented(YesOrNo.NO)
                .caseDataLiP(caseDataLiP)
                .build();

            // When
            boolean result = HmcDataUtils.isWelshHearingTemplate(caseData);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnTrue_whenRespondentNoRepAndDefendantDQDocumentsWelsh() {
            // Given
            WelshLanguageRequirements req = WelshLanguageRequirements.builder()
                .documents(Language.WELSH)
                .build();
            Respondent1DQ respondent1DQ = Respondent1DQ.builder()
                .respondent1DQLanguage(req)
                .build();

            // -> isRespondentResponseBilingual() = false (ej: "ENGLISH")
            CaseDataLiP caseDataLiP = CaseDataLiP.builder()
                .respondent1LiPResponse(RespondentLiPResponse.builder()
                                            .respondent1ResponseLanguage("ENGLISH").build())
                .build();

            CaseData caseData = CaseData.builder()
                .applicant1Represented(YesOrNo.YES)
                .respondent1Represented(YesOrNo.NO)
                .respondent1DQ(respondent1DQ)
                .caseDataLiP(caseDataLiP)
                .build();

            // When
            boolean result = HmcDataUtils.isWelshHearingTemplate(caseData);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnFalse_whenApplicantYesRepAndRespondentYesRep() {
            // Given
            // Ninguno es NO => toda la expresión OR se evalúa a false
            CaseData caseData = CaseData.builder()
                .applicant1Represented(YesOrNo.YES)
                .respondent1Represented(YesOrNo.YES)
                .build();

            // When
            boolean result = HmcDataUtils.isWelshHearingTemplate(caseData);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalse_whenApplicantNoRepButNotBilingualNotDQ_andRespondentNoRepButNotBilingualNotDQ() {
            // Given
            CaseDataLiP caseDataLiP = CaseDataLiP.builder()
                .respondent1LiPResponse(RespondentLiPResponse.builder()
                                            .respondent1ResponseLanguage("ENGLISH").build())
                .build();

            CaseData caseData = CaseData.builder()
                .applicant1Represented(YesOrNo.NO)
                .claimantBilingualLanguagePreference(Language.ENGLISH.toString()) // => false
                .respondent1Represented(YesOrNo.NO)
                .caseDataLiP(caseDataLiP)
                // Sin Applicant1DQ ni Respondent1DQ que establezcan WELSH o BOTH
                .build();

            // When
            boolean result = HmcDataUtils.isWelshHearingTemplate(caseData);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnWrandawiad_whenTitleIsHearingLowercase() {
            // Given
            String input = "hearing";
            // When
            String result = HmcDataUtils.translateTitle(input);
            // Then
            assertEquals("Wrandawiad", result);
        }

        @Test
        void shouldReturnWrandawiad_whenTitleIsHearingUppercase() {
            // Given
            String input = "Hearing";
            // When
            String result = HmcDataUtils.translateTitle(input);
            // Then
            assertEquals("Wrandawiad", result);
        }

        @Test
        void shouldReturnDreial_whenTitleIsTrialLowercase() {
            // Given
            String input = "trial";
            // When
            String result = HmcDataUtils.translateTitle(input);
            // Then
            assertEquals("Dreial", result);
        }

        @Test
        void shouldReturnDreial_whenTitleIsTrialUppercase() {
            // Given
            String input = "Trial";
            // When
            String result = HmcDataUtils.translateTitle(input);
            // Then
            assertEquals("Dreial", result);
        }

        @Test
        void shouldReturnSameTitle_whenTitleIsNotHearingOrTrial() {
            // Given
            String input = "anythingElse";
            // When
            String result = HmcDataUtils.translateTitle(input);
            // Then
            assertEquals("anythingElse", result);
        }
    }
}
