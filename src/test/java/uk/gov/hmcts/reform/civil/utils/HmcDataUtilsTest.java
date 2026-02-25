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
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
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
        PartiesNotifiedResponses partiesNotified = new PartiesNotifiedResponses().setResponses(List.of());

        PartiesNotifiedResponse result = HmcDataUtils.getLatestHearingNoticeDetails(partiesNotified);

        assertNull(result);
    }

    @Test
    void getLatestPartiesNotifiedResponse_WhenNonEmptyList_ReturnsLatestResponse() {
        LocalDateTime now = LocalDateTime.now();

        var res1 = new PartiesNotifiedResponse().setServiceData(new PartiesNotifiedServiceData().setHearingLocation("loc-3"))
            .setResponseReceivedDateTime(now.minusDays(3)).setRequestVersion(1);
        var res2 = new PartiesNotifiedResponse().setServiceData(new PartiesNotifiedServiceData().setHearingLocation("loc-2"))
            .setResponseReceivedDateTime(now.minusDays(2)).setRequestVersion(2);
        var expected = new PartiesNotifiedResponse().setServiceData(new PartiesNotifiedServiceData().setHearingLocation("loc-1"))
            .setResponseReceivedDateTime(now.minusDays(1)).setRequestVersion(3);

        PartiesNotifiedResponses partiesNotified = new PartiesNotifiedResponses()
            .setResponses(List.of(res1, expected, res2));

        PartiesNotifiedResponse result = HmcDataUtils.getLatestHearingNoticeDetails(partiesNotified);

        assertEquals(result, expected);
    }

    @Test
    void getHearingResponseForRequestVersion_WhenEmptyList_ReturnsNull() {
        PartiesNotifiedResponses partiesNotified = new PartiesNotifiedResponses().setResponses(List.of());

        PartiesNotifiedResponse result = HmcDataUtils.getLatestHearingResponseForRequestVersion(partiesNotified, 1);

        assertNull(result);
    }

    @Test
    void getHearingResponseForRequestVersion_WhenNonEmptyList_ReturnsLatestResponse() {
        LocalDateTime now = LocalDateTime.now();

        var res1 = new PartiesNotifiedResponse().setServiceData(new PartiesNotifiedServiceData().setHearingLocation("loc-3"))
            .setResponseReceivedDateTime(now.minusDays(3)).setRequestVersion(1);
        var res2 = new PartiesNotifiedResponse().setServiceData(new PartiesNotifiedServiceData().setHearingLocation("loc-2"))
            .setResponseReceivedDateTime(now.minusDays(2)).setRequestVersion(3);
        var res3 = new PartiesNotifiedResponse().setServiceData(new PartiesNotifiedServiceData().setHearingLocation("loc-1"))
            .setResponseReceivedDateTime(now.minusDays(1)).setRequestVersion(2);

        PartiesNotifiedResponses partiesNotified = new PartiesNotifiedResponses()
            .setResponses(List.of(res3, res1, res2));

        PartiesNotifiedResponse result = HmcDataUtils.getLatestHearingResponseForRequestVersion(partiesNotified, 3);

        assertEquals(result, res2);
    }

    @Test
    void hearingDataChanged_WhenHearingDataChanged_ReturnsTrue() {
        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(
                new HearingResponse().setHearingDaySchedule(
                        List.of(
                            new HearingDaySchedule()
                                .setHearingVenueId("Venue A")
                                .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
                                .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 11, 0)),
                            new HearingDaySchedule()
                                .setHearingVenueId("Venue A")
                                .setHearingStartDateTime(LocalDateTime.of(2023, 12, 24, 10, 0))
                                .setHearingEndDateTime(LocalDateTime.of(2023, 12, 24, 11, 0)))));

        PartiesNotifiedResponse partiesNotified = new PartiesNotifiedResponse()
            .setServiceData(new PartiesNotifiedServiceData()
                             .setDays(List.of(
                                 new HearingDay()
                                     .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
                                     .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 11, 0)),
                                 new HearingDay()
                                     .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
                                     .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 11, 0))))
                             .setHearingLocation("Venue A"));

        boolean result = HmcDataUtils.hearingDataChanged(partiesNotified, hearing);

        assertTrue(result);
    }

    @Test
    void hearingDataChanged_WhenHearingDataChanged_ReturnsTrueExtraDay() {
        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(
                new HearingResponse().setHearingDaySchedule(
                        List.of(
                            new HearingDaySchedule()
                                .setHearingVenueId("Venue A")
                                .setHearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0)),
                            new HearingDaySchedule()
                                .setHearingVenueId("Venue A")
                                .setHearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0)))));

        PartiesNotifiedResponse partiesNotified = new PartiesNotifiedResponse()
            .setServiceData(new PartiesNotifiedServiceData()
                             .setHearingDate(LocalDateTime.of(2023, 12, 23, 10, 0))
                             .setHearingLocation("Venue B")
                             .setDays(List.of(new HearingDay()
                                               .setHearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))))
                             .setHearingLocation("Venue A"));

        boolean result = HmcDataUtils.hearingDataChanged(partiesNotified, hearing);

        assertTrue(result);
    }

    @Test
    void hearingDataChanged_WhenHearingDataNotChanged_ReturnsFalse() {
        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(
                new HearingResponse().setHearingDaySchedule(
                        List.of(
                            new HearingDaySchedule()
                                .setHearingVenueId("Venue A")
                                .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
                                .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 11, 0)))));

        PartiesNotifiedResponse partiesNotified = new PartiesNotifiedResponse()
            .setServiceData(new PartiesNotifiedServiceData()
                             .setDays(List.of(new HearingDay()
                                               .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
                                               .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 11, 0))))
                             .setHearingLocation("Venue A"));

        boolean result = HmcDataUtils.hearingDataChanged(partiesNotified, hearing);

        assertFalse(result);
    }

    @Test
    void hearingDataChanged_WhenPartiesNotifiedIsNull_ReturnsTrue() {
        HearingGetResponse hearing = new HearingGetResponse();

        boolean result = HmcDataUtils.hearingDataChanged(null, hearing);

        assertTrue(result);
    }

    @Test
    void hearingDataChanged_WhenPartiesNotifiedServiceDataIsNull_ReturnsTrue() {
        HearingGetResponse hearing = new HearingGetResponse();
        PartiesNotifiedResponse partiesNotified = new PartiesNotifiedResponse();

        boolean result = HmcDataUtils.hearingDataChanged(partiesNotified, hearing);

        assertTrue(result);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 13, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 24, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 24, 16, 0));

        var hearingDay3 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 25, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 25, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)));

        var result = HmcDataUtils.getHearingDaysText(hearing, isWelsh);

        assertEquals(result, isWelsh ? """
            23 Rhagfyr 2023 am 10:00 am 3 awr
            24 Rhagfyr 2023 am 14:00 am 2 awr
            25 Rhagfyr 2023 am 10:00 am 6 awr"""
            : """
            23 December 2023 at 10:00 for 3 hours
            24 December 2023 at 14:00 for 2 hours
            25 December 2023 at 10:00 for 6 hours"""
        );
    }

    @Test
    void getTitle() {
    }

    @Nested
    class GetHearingStartDay {

        @Test
        void shouldReturnTheExpectedFirstDay() {
            var expected = new HearingDaySchedule()
                .setHearingStartDateTime(LocalDateTime.of(2023, 01, 01, 0, 0, 0));

            var hearing = new HearingGetResponse()
                .setHearingResponse(
                    new HearingResponse()
                        .setHearingDaySchedule(List.of(
                            new HearingDaySchedule()
                                .setHearingStartDateTime(
                                    LocalDateTime.of(2023, 01, 03, 0, 0, 0)),
                            expected,
                            new HearingDaySchedule()
                                .setHearingStartDateTime(
                                    LocalDateTime.of(2023, 01, 02, 0, 0, 0))
                        )));

            assertEquals(HmcDataUtils.getHearingStartDay(hearing), expected);
        }

        @Test
        void shouldReturnNullIfHearingIsNull() {
            assertNull(HmcDataUtils.getHearingStartDay(null));
        }

        @Test
        void shouldReturnNullIfHearingResponseIsNull() {
            var hearing = new HearingGetResponse();
            assertNull(HmcDataUtils.getHearingStartDay(hearing));
        }

        @Test
        void shouldReturnNullIfHearingResponseScheduleDaysAreNull() {
            var hearing = new HearingGetResponse()
                .setHearingResponse(new HearingResponse());
            assertNull(HmcDataUtils.getHearingStartDay(hearing));
        }

        @Test
        void shouldReturnNullIfHearingResponseScheduleDaysAreEmpty() {
            var hearing = new HearingGetResponse()
                .setHearingResponse(
                    new HearingResponse().setHearingDaySchedule(new ArrayList<>()))
                                ;
            assertNull(HmcDataUtils.getHearingStartDay(hearing));
        }
    }

    @Nested
    class GetHearingDays {

        @Test
        void shouldReturnExpectedHearingDays() {
            var hearingDayOne = new HearingDay()
                .setHearingStartDateTime(
                    LocalDateTime.of(2023, 01, 03, 0, 0, 0))
                .setHearingEndDateTime(
                    LocalDateTime.of(2023, 01, 03, 12, 0, 0));

            var hearingDayTwo = new HearingDay()
                .setHearingStartDateTime(
                    LocalDateTime.of(2023, 01, 05, 0, 0, 0))
                .setHearingEndDateTime(
                    LocalDateTime.of(2023, 01, 05, 12, 0, 0));

            var hearing = new HearingGetResponse()
                .setHearingResponse(
                    new HearingResponse()
                        .setHearingDaySchedule(List.of(
                            new HearingDaySchedule()
                                .setHearingStartDateTime(hearingDayOne.getHearingStartDateTime())
                                .setHearingEndDateTime(hearingDayOne.getHearingEndDateTime()),
                            new HearingDaySchedule()
                                .setHearingStartDateTime(hearingDayTwo.getHearingStartDateTime())
                                .setHearingEndDateTime(hearingDayTwo.getHearingEndDateTime())
                        )));

            assertEquals(HmcDataUtils.getHearingDays(hearing), List.of(hearingDayOne, hearingDayTwo));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_1Day_FullDay_BstHearingDay(Boolean isWelsh) {
        var hearingDay = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(List.of(hearingDay)));

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        assertEquals(result, List.of(isWelsh ? "23 Mai 2023 am 11:00 am 6 awr" : "23 May 2023 at 11:00 for 6 hours"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_1Day_FullDay(Boolean isWelsh) {
        var hearingDay = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(List.of(hearingDay)));

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        assertEquals(result, List.of(isWelsh ? "23 Rhagfyr 2023 am 10:00 am 6 awr" : "23 December 2023 at 10:00 for 6 hours"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_1Day_Morning(Boolean isWelsh) {
        var hearingDay = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 13, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(List.of(hearingDay)));

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        assertEquals(result, List.of(isWelsh ? "23 Rhagfyr 2023 am 10:00 am 3 awr" : "23 December 2023 at 10:00 for 3 hours"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_1Day_Afternoon(Boolean isWelsh) {
        var hearingDay = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(List.of(hearingDay)));

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        assertEquals(result, List.of(isWelsh ? "23 Rhagfyr 2023 am 14:00 am 2 awr" : "23 December 2023 at 14:00 for 2 hours"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_2Days_MorningAndAfternoon_BST(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 23, 13, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 24, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 24, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2)));

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedList = isWelsh
            ? List.of(
            "23 Mai 2023 am 11:00 am 3 awr",
            "24 Mai 2023 am 15:00 am 2 awr")
            : List.of(
            "23 May 2023 at 11:00 for 3 hours",
            "24 May 2023 at 15:00 for 2 hours");

        assertEquals(result, expectedList);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_2Days_MorningAndAfternoon(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 13, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 24, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 24, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2)));

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedList = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 10:00 am 3 awr",
            "24 Rhagfyr 2023 am 14:00 am 2 awr")
            : List.of(
            "23 December 2023 at 10:00 for 3 hours",
            "24 December 2023 at 14:00 for 2 hours");

        assertEquals(result, expectedList);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_2Days_FullDayAndAfternoon(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 24, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 24, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2)));

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 10:00 am 6 awr",
            "24 Rhagfyr 2023 am 14:00 am 2 awr")
            : List.of(
            "23 December 2023 at 10:00 for 6 hours",
            "24 December 2023 at 14:00 for 2 hours");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_2Days_FullDayAndMorning(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 24, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 24, 13, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2)));

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 10:00 am 6 awr",
            "24 Rhagfyr 2023 am 10:00 am 3 awr")
            : List.of(
            "23 December 2023 at 10:00 for 6 hours",
            "24 December 2023 at 10:00 for 3 hours");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_2Days_Morning(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 13, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 24, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 24, 13, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2)));

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 10:00 am 3 awr",
            "24 Rhagfyr 2023 am 10:00 am 3 awr")
            : List.of(
            "23 December 2023 at 10:00 for 3 hours",
            "24 December 2023 at 10:00 for 3 hours");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_2Days_Afternoon(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 24, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 24, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2)));

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 14:00 am 2 awr",
            "24 Rhagfyr 2023 am 14:00 am 2 awr")
            : List.of(
            "23 December 2023 at 14:00 for 2 hours",
            "24 December 2023 at 14:00 for 2 hours");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_3Days_FullDays(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 24, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 24, 16, 0));

        var hearingDay3 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 25, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 25, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)));

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 10:00 am 6 awr",
            "24 Rhagfyr 2023 am 10:00 am 6 awr",
            "25 Rhagfyr 2023 am 10:00 am 6 awr")
            : List.of(
            "23 December 2023 at 10:00 for 6 hours",
            "24 December 2023 at 10:00 for 6 hours",
            "25 December 2023 at 10:00 for 6 hours");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_3Days_FullDayMorningAndAfternoon(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 24, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 24, 13, 0));

        var hearingDay3 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 25, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 25, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)));

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 10:00 am 6 awr",
            "24 Rhagfyr 2023 am 10:00 am 3 awr",
            "25 Rhagfyr 2023 am 14:00 am 2 awr")
            : List.of(
            "23 December 2023 at 10:00 for 6 hours",
            "24 December 2023 at 10:00 for 3 hours",
            "25 December 2023 at 14:00 for 2 hours");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_3Days_Afternoon(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 24, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 24, 16, 0));

        var hearingDay3 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 25, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 25, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)));

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 14:00 am 2 awr",
            "24 Rhagfyr 2023 am 14:00 am 2 awr",
            "25 Rhagfyr 2023 am 14:00 am 2 awr")
            : List.of(
            "23 December 2023 at 14:00 for 2 hours",
            "24 December 2023 at 14:00 for 2 hours",
            "25 December 2023 at 14:00 for 2 hours");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_1Hour30minutes(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 14, 30))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1)));

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ?  List.of("23 Rhagfyr 2023 am 14:30 am 1 awr a 30 munud")
            : List.of("23 December 2023 at 14:30 for 1 hour and 30 minutes");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_30minutes(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 14, 30));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1)));

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedList = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 14:00 am 30 munud")
            : List.of(
            "23 December 2023 at 14:00 for 30 minutes");
        assertEquals(result, expectedList);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_1Hour15minutes(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 14, 45))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1)));

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 14:45 am 1 awr a 15 munud")
            : List.of(
            "23 December 2023 at 14:45 for 1 hour and 15 minutes");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getHearingDaysText_shouldReturnExpectedText_1Hour45minutes(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 12, 23, 14, 15))
            .setHearingEndDateTime(LocalDateTime.of(2023, 12, 23, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1)));

        var result = HmcDataUtils.getHearingDaysTextList(hearing, isWelsh);

        List<String> expectedResult = isWelsh
            ? List.of(
            "23 Rhagfyr 2023 am 14:15 am 1 awr a 45 munud")
            : List.of(
            "23 December 2023 at 14:15 for 1 hour and 45 minutes");

        assertEquals(result, expectedResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_1Day_3Hours_30minutes(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 10, 23, 10, 00))
            .setHearingEndDateTime(LocalDateTime.of(2023, 10, 23, 16, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 10, 24, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 10, 24, 13, 30));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 diwrnod a 3 awr a 30 munud" : "1 day and 3 hours and 30 minutes");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_1Day_1Hours_30minutes(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 10, 23, 10, 00))
            .setHearingEndDateTime(LocalDateTime.of(2023, 10, 23, 14, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 10, 24, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 10, 24, 13, 30));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 diwrnod ac 1 awr a 30 munud" : "1 day and 1 hour and 30 minutes");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_whenDurationLessThan1Hour(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 10, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 10, 23, 10, 30));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "30 munud" : "30 minutes");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_whenDuration5Hours(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 10, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 10, 23, 15, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "5 awr" : "5 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_whenDuration5Hours30Minutes(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 10, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 10, 23, 15, 30));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "5 awr a 30 munud" : "5 hours and 30 minutes");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_whenDuration5Hours20Minutes(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 10, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 10, 23, 15, 20));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "5 awr ac 20 munud" : "5 hours and 20 minutes");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_whenDuration1Day5Hours(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 10, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 10, 23, 15, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "5 awr" : "5 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_whenDuration1day30Minutes(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 10, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 10, 23, 16, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 10, 24, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 10, 24, 10, 30));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 diwrnod a 30 munud" : "1 day and 30 minutes");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_whenDuration1Day5Hours30Minutes(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 10, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 10, 23, 16, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 10, 24, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 10, 24, 15, 30));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 diwrnod a 5 awr a 30 munud" : "1 day and 5 hours and 30 minutes");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_1Day_FullDay(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 diwrnod" : "1 day");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_1Day_Morning(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 23, 13, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "3 awr" : "3 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_1Day_Afternoon(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 23, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "2 awr" : "2 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_2Days_MorningAndAfternoon(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 23, 13, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 24, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 24, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "5 awr" : "5 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_2Days_FullDayAndAfternoon(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 24, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 24, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 diwrnod a 2 awr" : "1 day and 2 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_2Days_FullDayAndMorning(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 24, 13, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 diwrnod a 3 awr" : "1 day and 3 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_2Days_Morning(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 23, 13, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 24, 13, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 diwrnod" : "1 day");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_2Days_48Hours(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 23, 13, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 24, 13, 0));

        var hearingDay3 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 25, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 25, 13, 0));

        var hearingDay4 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 26, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 26, 13, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3, hearingDay4)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "2 ddiwrnod" : "2 days");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_2Days_Afternoon(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 23, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 24, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 24, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "4 awr" : "4 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_3Days_FullDays(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 24, 16, 0));

        var hearingDay3 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 25, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 25, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "3 diwrnod" : "3 days");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_3Days_FullDayMorningAndAfternoon(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 23, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 24, 10, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 24, 13, 0));

        var hearingDay3 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 25, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 25, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 diwrnod a 5 awr" : "1 day and 5 hours");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getTotalHearingDurationText_3Days_Afternoon(Boolean isWelsh) {
        var hearingDay1 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 23, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 23, 16, 0));

        var hearingDay2 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 24, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 24, 16, 0));

        var hearingDay3 = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2023, 5, 25, 14, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 5, 25, 16, 0));

        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                List.of(hearingDay1, hearingDay2, hearingDay3)));

        var result = HmcDataUtils.getTotalHearingDurationText(hearing, isWelsh);

        assertEquals(result, isWelsh ? "1 diwrnod" : "1 day");
    }

    @Nested
    class IncludesVideoHearing {
        @Test
        void shouldReturnFalseIfCaseHearingsIsNull() {
            HearingsResponse hearings = new HearingsResponse();

            boolean actual = includesVideoHearing(hearings);

            assertFalse(actual);
        }

        @Test
        void shouldReturnFalseIfNoCaseHearingsExist() {
            HearingsResponse hearings = new HearingsResponse().setCaseHearings(List.of());

            boolean actual = includesVideoHearing(hearings);

            assertFalse(actual);
        }

        @Test
        void shouldReturnFalseIfNoVideoHearingsExist() {
            HearingsResponse hearings = new HearingsResponse().setCaseHearings(List.of(
                new CaseHearing()
                    .setHearingDaySchedule(List.of(
                        new HearingDaySchedule()
                            .setAttendees(List.of(
                                new Attendees()
                                    .setHearingSubChannel(INTER),
                                new Attendees()
                                    .setHearingSubChannel(null)
                            ))))));

            boolean actual = includesVideoHearing(hearings);

            assertFalse(actual);
        }

        @Test
        void shouldReturnTrue_IfVideoHearingsExistOnASingleDay() {
            HearingsResponse hearings = new HearingsResponse().setCaseHearings(List.of(
                new CaseHearing()
                    .setHearingDaySchedule(List.of(
                        new HearingDaySchedule()
                            .setAttendees(List.of(
                                new Attendees()
                                    .setHearingSubChannel(VIDCVP),
                                new Attendees()
                                    .setHearingSubChannel(null)
                            ))))));

            boolean actual = includesVideoHearing(hearings);

            assertTrue(actual);
        }

        @Test
        void shouldReturnTrue_IfVideoHearingsExistOneDayWithinMultipleDays() {
            HearingsResponse hearings = new HearingsResponse().setCaseHearings(List.of(
                new CaseHearing()
                    .setHearingDaySchedule(List.of(
                        new HearingDaySchedule()
                            .setAttendees(List.of(
                                new Attendees()
                                    .setHearingSubChannel(INTER),
                                new Attendees()
                                    .setHearingSubChannel(null)
                            )),
                        new HearingDaySchedule()
                            .setAttendees(List.of(
                                new Attendees()
                                    .setHearingSubChannel(VIDCVP),
                                new Attendees()
                                    .setHearingSubChannel(null)
                            ))))));

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
            List<LocationRefData> locations = List.of(new LocationRefData().setEpimmsId("venue"));
            when(locationRefDataService.getHearingCourtLocations("authToken"))
                .thenReturn(locations);
            LocationRefData locationRefData = HmcDataUtils.getLocationRefData(
                "HER123",
                "venue",
                "authToken",
                locationRefDataService
            );

            assertThat(locationRefData).isEqualTo(new LocationRefData().setEpimmsId("venue"));
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

            HearingsResponse hearingsResponse = new HearingsResponse()
                .setCaseHearings(List.of(
                    hearing("11111", TODAY.minusDays(1), List.of(hearingStartTime)),
                    hearing(hearingId, requestedDateTime, List.of(hearingStartTime)),
                    hearing("33333", TODAY.minusDays(3), List.of(hearingStartTime)),
                    hearing("22222", TODAY.minusDays(2), List.of(hearingStartTime))));

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

            String actual = HmcDataUtils.getHearingTypeTitleText(caseData, hearing, false);

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

            String actual = HmcDataUtils.getHearingTypeTitleText(caseData, hearing, false);

            assertEquals(expected, actual);
        }

        @ParameterizedTest
        @CsvSource({
            "TRI, SMALL_CLAIM, wrandawiad",
            "TRI, FAST_CLAIM, dreial",
            "DRH, SMALL_CLAIM, wrandawiad datrys anghydfod",
            "DIS, SMALL_CLAIM, wrandawiad gwaredu"
        })
        void shouldReturnExpectedTitleWelsh_specClaim(String hearingType, AllocatedTrack allocatedTrack, String expected) {
            HearingGetResponse hearing = buildHearing(hearingType);
            CaseData caseData = CaseData.builder().responseClaimTrack(allocatedTrack.name()).build();

            String actual = HmcDataUtils.getHearingTypeTitleText(caseData, hearing, true);

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

            String actual = HmcDataUtils.getHearingTypeContentText(caseData, hearing, false);

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

            String actual = HmcDataUtils.getHearingTypeContentText(caseData, hearing, false);

            assertEquals(expected, actual);
        }

        @ParameterizedTest
        @CsvSource({
            "TRI, SMALL_CLAIM, gwrandawiad",
            "TRI, FAST_CLAIM, treial",
            "DRH, SMALL_CLAIM, gwrandawiad",
            "DIS, FAST_CLAIM, gwrandawiad",
        })
        void shouldReturnExpectedTextWelsh_specClaim(String hearingType, AllocatedTrack allocatedTrack, String expected) {
            HearingGetResponse hearing = buildHearing(hearingType);
            CaseData caseData = CaseData.builder().responseClaimTrack(allocatedTrack.name()).build();

            String actual = HmcDataUtils.getHearingTypeContentText(caseData, hearing, true);

            assertEquals(expected, actual);
        }

        @ParameterizedTest
        @CsvSource({
            "TRI, SMALL_CLAIM, wrandawiadau",
            "TRI, FAST_CLAIM, dreialon",
            "DRH, SMALL_CLAIM, wrandawiadau",
            "DIS, SMALL_CLAIM, wrandawiadau",
        })
        void shouldReturnExpectedPluralTextWelsh_specClaim(String hearingType, AllocatedTrack allocatedTrack, String expected) {
            HearingGetResponse hearing = buildHearing(hearingType);
            CaseData caseData = CaseData.builder().responseClaimTrack(allocatedTrack.name()).build();

            String actual = HmcDataUtils.getPluralHearingTypeTextWelsh(caseData, hearing);

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
            PartyDetailsModel organisationParty = new PartyDetailsModel();
            organisationParty.setHearingSubChannel(INTER.name());
            organisationParty.setPartyID("PARTYID");
            organisationParty.setOrganisationDetails(buildOrganisationDetails("ID", "Misplaced Org"));
            HearingGetResponse hearing = buildHearingWithOrganisation(
                List.of(HearingIndividual.attendingHearingInPerson("Jason", "Wells"),
                        HearingIndividual.attendingHearingByPhone("Chloe", "Landale"),
                        HearingIndividual.attendingHearingInPerson("Michael", "Carver"),
                        HearingIndividual.attendingHearingByVideo("Jenny", "Harper"),
                        HearingIndividual.attendingHearingInPerson("Jack", "Crawley")
                ),
                organisationParty
            );

            String actual = HmcDataUtils.getInPersonAttendeeNames(hearing);

            assertEquals("Jason Wells\nMichael Carver\nJack Crawley", actual);
        }

        private OrganisationDetailsModel buildOrganisationDetails(String id, String name) {
            OrganisationDetailsModel organisationDetailsModel = new OrganisationDetailsModel();
            organisationDetailsModel.setCftOrganisationID(id);
            organisationDetailsModel.setName(name);
            return organisationDetailsModel;
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
        return new HearingGetResponse()
            .setPartyDetails(testIndividuals.stream().map(HearingIndividual::buildPartyDetails).toList())
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(List.of(
                new HearingDaySchedule()
                    .setAttendees(testIndividuals.stream().map(HearingIndividual::buildAttendee).toList()))));
    }

    private HearingGetResponse buildHearing(String hearingType) {
        return new HearingGetResponse()
            .setHearingDetails(new HearingDetails().setHearingType(hearingType));
    }

    private HearingGetResponse buildHearingWithOrganisation(List<HearingIndividual> testIndividuals,
                                                            PartyDetailsModel org) {
        List<PartyDetailsModel> partyDetails = new ArrayList<>(testIndividuals.stream()
                                                                   .map(HearingIndividual::buildPartyDetails).toList());

        partyDetails.add(org);

        return new HearingGetResponse()
            .setPartyDetails(partyDetails)
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(List.of(
                new HearingDaySchedule()
                    .setAttendees(testIndividuals.stream().map(HearingIndividual::buildAttendee).toList()))));
    }

    private CaseHearing hearing(String hearingId, LocalDateTime hearingRequestTime, List<LocalDateTime> startTimes) {
        return new CaseHearing()
            .setHearingId(Long.valueOf(hearingId))
            .setHearingRequestDateTime(hearingRequestTime)
            .setHearingDaySchedule(startTimes.stream().map(startTime -> new HearingDaySchedule().setHearingStartDateTime(
                startTime)).collect(
                Collectors.toList()));
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
            Applicant1DQ applicant1DQ = new Applicant1DQ()
                .setApplicant1DQLanguage(null);
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
            WelshLanguageRequirements req = new WelshLanguageRequirements()
                .setDocuments(Language.ENGLISH);
            Applicant1DQ applicant1DQ = new Applicant1DQ()
                .setApplicant1DQLanguage(req);
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
            WelshLanguageRequirements req = new WelshLanguageRequirements()
                .setDocuments(Language.WELSH);
            Applicant1DQ applicant1DQ = new Applicant1DQ()
                .setApplicant1DQLanguage(req);
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
            WelshLanguageRequirements req = new WelshLanguageRequirements()
                .setDocuments(Language.BOTH);
            Applicant1DQ applicant1DQ = new Applicant1DQ()
                .setApplicant1DQLanguage(req);
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
            Respondent1DQ respondent1DQ = new Respondent1DQ()
                .setRespondent1DQLanguage(null);
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
            WelshLanguageRequirements req = new WelshLanguageRequirements()
                .setDocuments(Language.ENGLISH);
            Respondent1DQ respondent1DQ = new Respondent1DQ()
                .setRespondent1DQLanguage(req);
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
            WelshLanguageRequirements req = new WelshLanguageRequirements()
                .setDocuments(Language.WELSH);
            Respondent1DQ respondent1DQ = new Respondent1DQ()
                .setRespondent1DQLanguage(req);
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
            WelshLanguageRequirements req = new WelshLanguageRequirements()
                .setDocuments(Language.BOTH);
            Respondent1DQ respondent1DQ = new Respondent1DQ()
                .setRespondent1DQLanguage(req);
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
            WelshLanguageRequirements req = new WelshLanguageRequirements()
                .setDocuments(Language.BOTH);
            Applicant1DQ dq = new Applicant1DQ()
                .setApplicant1DQLanguage(req);

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
            CaseDataLiP caseDataLiP = new CaseDataLiP()
                .setRespondent1LiPResponse(new RespondentLiPResponse()
                                            .setRespondent1ResponseLanguage("BOTH"));

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
            WelshLanguageRequirements req = new WelshLanguageRequirements()
                .setDocuments(Language.WELSH);
            Respondent1DQ respondent1DQ = new Respondent1DQ()
                .setRespondent1DQLanguage(req);

            // -> isRespondentResponseBilingual() = false (ej: "ENGLISH")
            CaseDataLiP caseDataLiP = new CaseDataLiP()
                .setRespondent1LiPResponse(new RespondentLiPResponse()
                                            .setRespondent1ResponseLanguage("ENGLISH"));

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
            CaseDataLiP caseDataLiP = new CaseDataLiP()
                .setRespondent1LiPResponse(new RespondentLiPResponse()
                                            .setRespondent1ResponseLanguage("ENGLISH"));

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
    }
}
