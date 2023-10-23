package uk.gov.hmcts.reform.civil.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingDuration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.HearingNotes;
import uk.gov.hmcts.reform.civil.model.NextHearingDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SDOHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingNotesDJ;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingHearingNotesDJ;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearings.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.hearings.HearingsResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.EXCEPTION;
import static uk.gov.hmcts.reform.hmc.model.messaging.HmcStatus.LISTED;

public class HearingUtilsTest {

    private static final LocalDateTime CURRENT_DATE = LocalDateTime.of(2024, 01, 06, 0, 0, 0);
    private static MockedStatic currentDateMock;

    @SuppressWarnings("unchecked")
    @BeforeAll
    static void setupSuite() {
        currentDateMock = mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS);
        currentDateMock.when(LocalDateTime::now).thenReturn(CURRENT_DATE);
    }

    @SuppressWarnings("unchecked")
    @AfterAll
    static void tearDown() {
        currentDateMock.reset();
    }

    @Test
    void shouldThrowNullException_whenGivenNullDate() {
        assertThrows(IllegalArgumentException.class, () ->
            HearingUtils.addBusinessDays(null, 0, null));
    }

    @Test
    void shouldReturnLocalDate_whenGivenAnyDate() {
        Set<LocalDate> holidaySet = new HashSet<>();
        holidaySet.add(LocalDate.now().plusDays(5));
        LocalDate days = HearingUtils.addBusinessDays(LocalDate.now(), 10, holidaySet);
        assertThat(days).isNotNull();
    }

    @DisplayName("HearingUtils.formatHearingFee should return <null> when the hearing fee is zero.")
    void shouldReturnNull_when0ClaimFee() {
        assertThat(HearingUtils.formatHearingFee(
            Fee.builder().calculatedAmountInPence(new BigDecimal(0)).build())).isNull();
    }

    @ParameterizedTest
    @CsvSource(value = {"34600;£346", "132000;£1,320", "5000;£50"}, delimiter = ';')
    @DisplayName("HearingUtils.formatHearingFee should format an amount in pence into a pound value."
        + " Fractional values can be discarded.")
    void shouldReturnFormattedFee_whenGivenAnyClaimFee(int amount, String expectedOutput) {
        BigDecimal feeAmount = new BigDecimal(amount);
        assertThat(HearingUtils.formatHearingFee(
            Fee.builder().calculatedAmountInPence(feeAmount).build())).isEqualTo(expectedOutput);
    }

    @ParameterizedTest
    @EnumSource(HearingDuration.class)
    void shouldReturnHearingDuration_whenGivenAnyHearingDuration(HearingDuration hearingDuration) {
        assertThat(HearingUtils.formatHearingDuration(hearingDuration)).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"000", "50000", "08:00", "8:00", "12:00", "23:00"})
    @DisplayName("HearingUtils.getHearingTimeFormatted should not allow invalid values. Valid values"
        + " are composed of 4 numerical digits only.")
    void shouldReturnNull_whenNotAllowedTime(String input) {
        assertThat(HearingUtils.getHearingTimeFormatted(input)).isNull();
    }

    @Test
    @DisplayName("HearingUtils.getHearingTimeFormatted should return <null> when an empty string is passed.")
    void shouldReturnNull_whenGivenEmptyTime() {
        assertThat(HearingUtils.getHearingTimeFormatted("")).isNull();
    }

    @ParameterizedTest
    @CsvSource(value = {"0000;00:00", "0500;05:00", "1200;12:00", "2300;23:00",
        "1230;12:30", "2318;23:18"}, delimiter = ';')
    @DisplayName("HearingUtils.getHearingTimeFormatted should put a ':' between a 4-digit value"
        + " as they are considered to be hours.")
    void shouldReturnTimedFormatted_whenGivenAnyTime(String input, String expectedOutput) {
        assertThat(HearingUtils.getHearingTimeFormatted(input)).isEqualTo(expectedOutput);
    }

    @Test
    void shouldReturnExpectedHearingNotes_whenDisposalHearingHearingNotesAreProvided() {
        HearingNotes expected = HearingNotes.builder()
            .notes("test notes")
            .date(LocalDate.now())
            .build();
        CaseData caseData = CaseData.builder()
            .disposalHearingHearingNotes("test notes")
            .build();

        HearingNotes actual = HearingUtils.getHearingNotes(caseData);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnExpectedHearingNotes_whenFastTrackHearingNotesAreProvided() {
        HearingNotes expected = HearingNotes.builder()
            .notes("test notes")
            .date(LocalDate.now())
            .build();
        CaseData caseData = CaseData.builder()
            .fastTrackHearingNotes(FastTrackHearingNotes.builder().input("test notes").build())
            .build();

        HearingNotes actual = HearingUtils.getHearingNotes(caseData);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnExpectedHearingNotes_whenDisposalHearingHearingNotesDJAreProvided() {
        HearingNotes expected = HearingNotes.builder()
            .notes("test notes")
            .date(LocalDate.now())
            .build();
        CaseData caseData = CaseData.builder()
            .disposalHearingHearingNotesDJ(DisposalHearingHearingNotesDJ.builder().input("test notes").build())
            .build();

        HearingNotes actual = HearingUtils.getHearingNotes(caseData);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnExpectedHearingNotes_whenSdoHearingNotesAreProvided() {
        HearingNotes expected = HearingNotes.builder()
            .notes("test notes")
            .date(LocalDate.now())
            .build();
        CaseData caseData = CaseData.builder()
            .sdoHearingNotes(SDOHearingNotes.builder().input("test notes").build())
            .build();

        HearingNotes actual = HearingUtils.getHearingNotes(caseData);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnExpectedHearingNotes_whenTrialHearingHearingNotesDJAreProvided() {
        HearingNotes expected = HearingNotes.builder()
            .notes("test notes")
            .date(LocalDate.now())
            .build();
        CaseData caseData = CaseData.builder()
            .trialHearingHearingNotesDJ(TrialHearingHearingNotesDJ.builder().input("test notes").build())
            .build();

        HearingNotes actual = HearingUtils.getHearingNotes(caseData);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnNull_whenNoSupportedNoteFieldsAreProvided() {
        CaseData caseData = CaseData.builder().build();

        HearingNotes actual = HearingUtils.getHearingNotes(caseData);

        assertThat(actual).isEqualTo(null);
    }

    @Test
    void shouldReturnClaimantVDefendant_whenIs1v1Claim() {
        // Given
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder().individualLastName("Doe").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().companyName("Company").type(Party.Type.COMPANY).build())
            .build();
        // When
        String claimantVDefendant = HearingUtils.getClaimantVDefendant(caseData);
        // Then
        assertThat(claimantVDefendant).isEqualTo("Doe v Company");
    }

    @Nested
    class GetActiveHearing {
        @Test
        void shouldReturnActiveHearing_whenAListedHearingExists() {
            CaseHearing canceledHearing = CaseHearing.builder()
                .hmcStatus(CANCELLED.name())
                .build();
            CaseHearing exceptionHearing = CaseHearing.builder()
                .hmcStatus(EXCEPTION.name())
                .build();
            CaseHearing listedHearing = CaseHearing.builder()
                .hmcStatus(LISTED.name())
                .build();

            HearingsResponse hearingsResponse = HearingsResponse.builder()
                .caseHearings(
                    List.of(
                        canceledHearing,
                        exceptionHearing,
                        listedHearing
                    )
                ).build();

            CaseHearing actual = HearingUtils.getActiveHearing(hearingsResponse);

            assertEquals(listedHearing, actual);
        }

        @Test
        @SneakyThrows
        void shouldThrowIllegalArgumentException_whenAListedHearingDoesNotExist() {
            CaseHearing canceledHearing = CaseHearing.builder()
                .hmcStatus(CANCELLED.name())
                .build();
            CaseHearing exceptionHearing = CaseHearing.builder()
                .hmcStatus(EXCEPTION.name())
                .build();

            HearingsResponse hearingsResponse = HearingsResponse.builder()
                .caseHearings(
                    List.of(
                        canceledHearing,
                        exceptionHearing
                    )
                ).build();

            assertThrows(IllegalArgumentException.class, () -> {
                HearingUtils.getActiveHearing(hearingsResponse);
            }, "No listed hearing was found.");
        }
    }

    @Nested
    class GetNextHearingDate {

        private final String hearingId = "12345";

        @Test
        void shouldReturnNull_forGivenHearingWithElapsedHearingDays() {
            LocalDateTime firstElapsedHearingDate = LocalDateTime.of(2024, 01, 04, 9, 00, 00);
            LocalDateTime secondElapsedHearingDate = LocalDateTime.of(2024, 01, 05, 9, 00, 00);

            CaseHearing hearing =
                CaseHearing.builder()
                    .hearingId(Long.valueOf(hearingId))
                    .hearingDaySchedule(List.of(
                        HearingDaySchedule.builder().hearingStartDateTime(firstElapsedHearingDate).build(),
                        HearingDaySchedule.builder().hearingStartDateTime(secondElapsedHearingDate).build()
                    ))
                    .build();

            LocalDateTime actual = HearingUtils.getNextHearingDate(hearing);

            assertNull(actual);
        }

        @Test
        void shouldReturnExpectedNextHearingDate_forGivenHearingWithANextHearingDateOfToday() {
            LocalDateTime elapsedHearingDate = LocalDateTime.of(2024, 01, 04, 9, 00, 00);
            LocalDateTime nextHearingDate = LocalDateTime.of(2024, 01, 10, 9, 00, 00);
            LocalDateTime futureHearingDate = LocalDateTime.of(2024, 01, 11, 9, 00, 00);

            CaseHearing hearing =
                CaseHearing.builder()
                    .hearingId(Long.valueOf(hearingId))
                    .hearingDaySchedule(List.of(
                        HearingDaySchedule.builder().hearingStartDateTime(elapsedHearingDate).build(),
                        HearingDaySchedule.builder().hearingStartDateTime(nextHearingDate).build(),
                        HearingDaySchedule.builder().hearingStartDateTime(futureHearingDate).build()
                    ))
                    .build();

            LocalDateTime actual = HearingUtils.getNextHearingDate(hearing);

            assertEquals(nextHearingDate, actual);
        }

        @Test
        void shouldReturnExpectedNextHearingDate_forGivenHearingWithFutureHearings() {
            LocalDateTime elapsedHearingDate = LocalDateTime.of(2024, 01, 04, 9, 00, 00);
            LocalDateTime nextHearingDate = LocalDateTime.of(2024, 01, 11, 9, 00, 00);
            LocalDateTime futureHearingDate = LocalDateTime.of(2024, 01, 12, 9, 00, 00);

            CaseHearing hearing =
                CaseHearing.builder()
                    .hearingId(Long.valueOf(hearingId))
                    .hearingDaySchedule(List.of(
                        HearingDaySchedule.builder().hearingStartDateTime(elapsedHearingDate).build(),
                        HearingDaySchedule.builder().hearingStartDateTime(nextHearingDate).build(),
                        HearingDaySchedule.builder().hearingStartDateTime(futureHearingDate).build()
                    ))
                    .build();

            LocalDateTime actual = HearingUtils.getNextHearingDate(hearing);

            assertEquals(nextHearingDate, actual);
        }
    }

    @Nested
    class GetNextHearingDetails {

        private final String hearingId = "12345";

        @Test
        void shouldReturnExpectedNextHearingDetails_forGivenHearingsWithFutureHearings() {
            LocalDateTime previousHearingDate = LocalDateTime.of(2024, 01, 5, 9, 00, 00);
            LocalDateTime nextHearingDate = LocalDateTime.of(2024, 01, 7, 9, 00, 00);

            HearingsResponse hearingsResponse = HearingsResponse.builder()
                .caseHearings(List.of(
                    CaseHearing.builder()
                        .hearingId(Long.valueOf(hearingId))
                        .hmcStatus(LISTED.name())
                        .hearingDaySchedule(List.of(
                            HearingDaySchedule.builder().hearingStartDateTime(previousHearingDate).build(),
                            HearingDaySchedule.builder().hearingStartDateTime(nextHearingDate).build()
                        )).build()))
                .build();

            NextHearingDetails actual = HearingUtils.getNextHearingDetails(hearingsResponse);
            NextHearingDetails expected = NextHearingDetails.builder()
                .hearingID(hearingId)
                .hearingDateTime(nextHearingDate)
                .build();

            assertEquals(expected, actual);
        }

        @Test
        void shouldReturnNull_forGivenHearingsWithElapsedHearings() {
            LocalDateTime firstElapsedHearingDay = LocalDateTime.of(2024, 01, 4, 9, 00, 00);
            LocalDateTime secondElapsedHearingDay = LocalDateTime.of(2024, 01, 5, 9, 00, 00);

            HearingsResponse hearingsResponse = HearingsResponse.builder()
                .caseHearings(List.of(
                    CaseHearing.builder()
                        .hearingId(Long.valueOf(hearingId))
                        .hmcStatus(LISTED.name())
                        .hearingDaySchedule(List.of(
                            HearingDaySchedule.builder().hearingStartDateTime(firstElapsedHearingDay).build(),
                            HearingDaySchedule.builder().hearingStartDateTime(secondElapsedHearingDay).build()
                        )).build()))
                .build();

            NextHearingDetails actual = HearingUtils.getNextHearingDetails(hearingsResponse);

            assertNull(actual);
        }

        @Test
        @SneakyThrows
        void shouldThrowIllegalArgumentException_whenNoListedHearingsExist() {
            HearingsResponse hearingsResponse = HearingsResponse.builder()
                .caseHearings(List.of(
                    CaseHearing.builder()
                        .hearingId(Long.valueOf(hearingId))
                        .hmcStatus(CANCELLED.name()).build()
                )).build();

            assertThrows(IllegalArgumentException.class, () -> {
                HearingUtils.getNextHearingDetails(hearingsResponse);
            }, "No listed hearing was found.");
        }
    }
}

