package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.helpers.ResourceReader;
import uk.gov.hmcts.reform.civil.bankholidays.BankHolidays;
import uk.gov.hmcts.reform.civil.bankholidays.BankHolidaysApi;
import uk.gov.hmcts.reform.civil.bankholidays.NonWorkingDaysCollection;
import uk.gov.hmcts.reform.civil.bankholidays.PublicHolidaysCollection;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.assertion.DayAssert.assertThat;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_DAY;

@ExtendWith(SpringExtension.class)
public class GeneralAppsDeadlinesCalculatorTest {

    @Mock
    private BankHolidaysApi bankHolidaysApi;
    @Mock
    private NonWorkingDaysCollection nonWorkingDaysCollection;

    private GeneralAppsDeadlinesCalculator calculator;

    @BeforeEach
    public void setUp() throws IOException {
        WorkingDayIndicator workingDayIndicator = new WorkingDayIndicator(
            new PublicHolidaysCollection(bankHolidaysApi),
            nonWorkingDaysCollection
        );
        when(bankHolidaysApi.retrieveAll()).thenReturn(loadFixture());

        calculator = new GeneralAppsDeadlinesCalculator(workingDayIndicator);
    }

    /**
     * The fixture is taken from the real bank holidays API.
     */
    private BankHolidays loadFixture() throws IOException {
        String input = ResourceReader.readString("/bank-holidays.json");
        return new ObjectMapper().readValue(input, BankHolidays.class);
    }

    @Nested
    class ApplicantResponseDeadlineDates {

        @Test
        void shouldReturnDeadlinePlus2Days_whenResponseDateIsWeekday() {
            LocalDateTime weekdayDate = LocalDate.of(2022, 2, 15).atTime(12, 0);
            LocalDateTime expectedDeadline = weekdayDate.toLocalDate().plusDays(2).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadline(weekdayDate, 2);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }

        @Test
        void shouldReturnDeadlinePlus3Days_whenResponseDateIsWeekday() {
            LocalDateTime weekdayDate = LocalDate.of(2022, 2, 15).atTime(12, 0);
            LocalDateTime expectedDeadline = weekdayDate.toLocalDate().plusDays(3).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadline(weekdayDate, 3);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }

        @Test
        void shouldReturnDeadlineWhenHourIsMoreThanOrEqualTo16_whenResponseDateIsWeekday() {
            LocalDateTime weekdayDate = LocalDate.of(2022, 2, 14).atTime(END_OF_DAY);
            LocalDateTime expectedDeadline = LocalDate.of(2022, 2, 17).atTime(END_OF_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadline(weekdayDate, 2);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }

        @Test
        void shouldReturnDeadlinePlus5WorkingDays_whenResponseDateIsWeekdayBefore4pm() {
            // Monday 9 Feb 2026 at 15:00 (3pm, before 4pm) starts from Mon 9 Feb
            // start date(9 Feb) + 4 calendar days + non-working days
            // Start: Mon 9 Feb, End: Fri 13 Feb (9+4=13)
            // Non-working days between 9-13 Feb: none (all weekdays)
            // Result: Fri 13 + 0 = Fri 13 Feb 2026 at 4pm
            // This gives 5 working days: Mon 9, Tue 10, Wed 11, Thu 12, Fri 13
            LocalDateTime weekdayDate = LocalDate.of(2026, 2, 9).atTime(15, 0);
            LocalDateTime expectedDeadline = LocalDate.of(2026, 2, 13).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadlineWithWeekendCheck(weekdayDate, 4);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }

        @Test
        void shouldReturnDeadlinePlus5WorkingDays_whenResponseDateIsWeekdayAfter4pm() {
            // Monday 9 Feb 2026 at 18:00 (6pm, after 4pm) starts from Tue 10 Feb
            // start date (9 Feb) + 4 calendar days + non-working days
            // Start: Tue 10 Feb, End: Sat 14 Feb (10+4=14)
            // Non-working days between 10-14 Feb: Sat 14 = 1 day
            // Result: Sun 15 moved to Mon 16 Feb 2026 at 4pm
            // This gives 5 working days: Tue 10, Wed 11, Thu 12, Fri 13, Mon 16
            LocalDateTime weekdayDate = LocalDate.of(2026, 2, 9).atTime(18, 0);
            LocalDateTime expectedDeadline = LocalDate.of(2026, 2, 16).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadlineWithWeekendCheck(weekdayDate, 4);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }

        @Test
        void shouldReturnDeadlinePlus5WorkingDays_whenResponseDateIsFridayBeforeWeekend() {
            // Friday 13 Feb 2026 at 15:00 (3pm, before 4pm)
            // Start: Fri 13, End: Tue 17 (13+4=17)
            // datesUntil(Fri 13, Wed 18) counts Sat 14, Sun 15, Mon 16, Tue 17
            // Non-working: Sat 14, Sun 15 = 2 days
            // Result: Tue 17 + 2 = Thu 19
            LocalDateTime fridayDate = LocalDate.of(2026, 2, 13).atTime(15, 0);
            LocalDateTime expectedDeadline = LocalDate.of(2026, 2, 19).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadlineWithWeekendCheck(fridayDate, 4);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }

        @Test
        void shouldReturnDeadlinePlus5WorkingDays_from26MarchBefore4pm() {
            // Thursday 26 Mar 2026 at 15:00 (3pm, before 4pm) starts counting from 26 Mar
            // start date + 4 calendar days + non-working days in range
            // Start: Thu 26 Mar, End: Mon 30 Mar (26+4=30)
            // Non-working days between 26-30 Mar: Sat 28, Sun 29 = 2 days
            // Result: 30 Mar + 2 days = Wed 1 Apr 2026 at 4pm
            // This gives 5 working days: Thu 26, Fri 27, Mon 30, Tue 31, Wed 1 Apr
            LocalDateTime applicationDate = LocalDate.of(2026, 3, 26).atTime(15, 0);
            LocalDateTime expectedDeadline = LocalDate.of(2026, 4, 1).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadlineWithWeekendCheck(applicationDate, 4);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }

        @Test
        void shouldReturnDeadlinePlus5WorkingDays_from26MarchAfter4pm() {
            // Thursday 26 Mar 2026 at 17:00 (5pm, after 4pm) starts counting from 27 Mar
            // start date + 4 calendar days + non-working days in range
            // Start: Fri 27 Mar, End: Tue 31 Mar (27+4=31)
            // Non-working days between 27-31 Mar: Sat 28, Sun 29 = 2 days
            // Result: 31 Mar + 2 days = Thu 2 Apr 2026 at 4pm
            // This gives 5 working days: Fri 27, Mon 30, Tue 31, Wed 1 Apr, Thu 2 Apr
            LocalDateTime applicationDate = LocalDate.of(2026, 3, 26).atTime(17, 0);
            LocalDateTime expectedDeadline = LocalDate.of(2026, 4, 2).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadlineWithWeekendCheck(applicationDate, 4);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }

        @Test
        void shouldReturnDeadlinePlus5WorkingDays_whenResultWouldFallOnSaturday() {
            // Tuesday 10 Feb 2026 at 15:00 (3pm, before 4pm) starts counting from 10 Feb
            // start date + 4 calendar days + non-working days in range
            // Start: Tue 10 Feb, End: Sat 14 Feb (10+4=14)
            // Non-working days between 10-14 Feb: Sat 14 = 1 day
            // Result: Sun 15 moved to Mon 16 Feb
            // This gives 5 working days: Tue 10, Wed 11, Thu 12, Fri 13, Mon 16
            LocalDateTime applicationDate = LocalDate.of(2026, 2, 10).atTime(15, 0);
            LocalDateTime expectedDeadline = LocalDate.of(2026, 2, 16).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadlineWithWeekendCheck(applicationDate, 4);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }

        @Test
        void shouldReturnDeadlinePlus5WorkingDays_whenResultWouldFallOnSunday() {
            // Wednesday 11 Feb 2026 at 15:00 (3pm, before 4pm) starts counting from 11 Feb
            // start date + 4 calendar days + non-working days in range
            // Start: Wed 11 Feb, End: Sun 15 Feb (11+4=15)
            // Non-working days between 11-15 Feb: Sat 14, Sun 15 = 2 days
            // Result: Tue 17 Feb (confirming Sunday is properly excluded)
            // This gives 5 working days: Wed 11, Thu 12, Fri 13, Mon 16, Tue 17
            LocalDateTime applicationDate = LocalDate.of(2026, 2, 11).atTime(15, 0);
            LocalDateTime expectedDeadline = LocalDate.of(2026, 2, 17).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadlineWithWeekendCheck(applicationDate, 4);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }
    }
}
