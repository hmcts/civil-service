package uk.gov.hmcts.reform.unspec.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.of;
import static java.time.Month.APRIL;
import static java.time.Month.AUGUST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.assertion.DayAssert.assertThat;

class IssueDateCalculatorTest {

    private static final boolean YES = true;
    private static final boolean NO = false;

    private static final LocalDateTime WEEKDAY_MORNING = of(2020, AUGUST, 03, 9, 30);
    private static final LocalDateTime WEEKDAY_EVENING = of(2020, AUGUST, 03, 16, 01);
    private static final LocalDateTime FRIDAY_EVENING = of(2020, AUGUST, 07, 16, 01);
    private static final LocalDateTime GOOD_FRIDAY_MORNING = of(2020, APRIL, 10, 9, 01);
    private static final LocalDateTime GOOD_THURSDAY_EVENING = of(2020, APRIL, 9, 16, 01);
    private static final LocalDateTime SAT_MORNING = of(2020, AUGUST, 1, 9, 30);
    private static final LocalDateTime SAT_EVENING = of(2020, AUGUST, 1, 16, 30);
    private static final LocalDateTime SUN_MORNING = of(2020, AUGUST, 2, 0, 1);
    private static final LocalDateTime SUN_EVENING = of(2020, AUGUST, 2, 17, 30);

    private IssueDateCalculator calculator;
    private WorkingDayIndicator workingDayIndicator;

    @BeforeEach
    void setUp() {
        workingDayIndicator = mock(WorkingDayIndicator.class);
        calculator = new IssueDateCalculator(workingDayIndicator);
    }

    @Nested
    class NoPublicHoliday {

        @Test
        void shouldBeIssuedTheSameDay_whenSubmittedOnWeekdayMorning() {
            noHolidays();

            LocalDate issueDate = calculator.calculateIssueDay(WEEKDAY_MORNING);

            assertThat(issueDate).isTheSame(WEEKDAY_MORNING).isWeekday();
        }

        @Test
        void shouldBeIssuedNextDay_whenSubmittedOnMondayEvening() {
            noHolidays();

            LocalDate issueDate = calculator.calculateIssueDay(WEEKDAY_EVENING);

            assertThat(issueDate).isTuesday().isNumberOfDaysSince(1, WEEKDAY_MORNING);
        }

        @Test
        void shouldBeIssuedAfterWeekend_whenSubmittedOnFridayEvening() {
            // Sat, Sun, Mon
            when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, YES);

            LocalDate issueDate = calculator.calculateIssueDay(FRIDAY_EVENING);

            assertThat(issueDate).isMonday().isNumberOfDaysSince(3, FRIDAY_EVENING);
        }

        @Test
        void shouldBeIssuedAfterWeekend_whenSubmittedOnSaturdayMorning() {
            // Sat, Sun, Mon
            when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, YES);

            LocalDate issueDate = calculator.calculateIssueDay(SAT_MORNING);

            assertThat(issueDate).isMonday().isNumberOfDaysSince(2, SAT_MORNING);
        }

        @Test
        void shouldBeIssuedNextDay_whenSubmittedOnSundayMorning() {
            // Sun, Mon
            when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, YES);

            LocalDate issueDate = calculator.calculateIssueDay(SUN_MORNING);

            assertThat(issueDate).isMonday().isNumberOfDaysSince(1, SUN_MORNING);
        }

        private void noHolidays() {
            when(workingDayIndicator.isWorkingDay(any())).thenReturn(true);
        }
    }

    @Nested
    class PublicHoliday {

        @Test
        void shouldBeIssuedOnTuesdayInFourDays_whenSubmittedOnGoodFridayMorning() {
            // Fri, Sat, Sun, Mon, Tue
            when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, NO, NO, YES);

            LocalDate issueDate = calculator.calculateIssueDay(GOOD_FRIDAY_MORNING);

            assertThat(issueDate).isTuesday().isNumberOfDaysSince(4, GOOD_FRIDAY_MORNING);
        }

        @Test
        void shouldBeIssuedOnTuesdayInFiveDays_whenSubmittedOnGoodThursdayEvening() {
            // Fri, Sat, Sun, Mon, Tue
            when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, NO, NO, YES);

            LocalDate issueDate = calculator.calculateIssueDay(GOOD_THURSDAY_EVENING);

            assertThat(issueDate).isTuesday().isNumberOfDaysSince(5, GOOD_THURSDAY_EVENING);
        }

        @Test
        void shouldBeIssuedOnTuesday_whenSubmittedOnSaturdayMorningBankHolidayOnMonday() {
            // Sat, Sun, Mon, Tue
            when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, NO, YES);

            LocalDate issueDate = calculator.calculateIssueDay(SAT_MORNING);

            assertThat(issueDate).isTuesday().isNumberOfDaysSince(3, SAT_MORNING);
        }

        @Test
        void shouldBeIssuedOnTuesday_whenSubmittedOnSaturdayEveningBankHolidayOnMonday() {
            // Sun, Mon, Tue
            when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, YES);

            LocalDate issueDate = calculator.calculateIssueDay(SAT_EVENING);

            assertThat(issueDate).isTuesday().isNumberOfDaysSince(3, SAT_EVENING);
        }

        @Test
        void shouldBeIssuedOnTuesday_whenSubmittedOnSundayMorningBankHolidayOnMonday() {
            // Sun, Mon, Tue
            when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, NO, YES);

            LocalDate issueDate = calculator.calculateIssueDay(SUN_MORNING);

            assertThat(issueDate).isTuesday().isNumberOfDaysSince(2, SUN_MORNING);
        }

        @Test
        void shouldBeIssuedOnTuesday_whenSubmittedOnSundayEveningBankHolidayOnMonday() {
            // Mon, Tue
            when(workingDayIndicator.isWorkingDay(any())).thenReturn(NO, YES);

            LocalDate issueDate = calculator.calculateIssueDay(SUN_EVENING);

            assertThat(issueDate).isTuesday().isNumberOfDaysSince(2, SUN_EVENING);
        }
    }
}
