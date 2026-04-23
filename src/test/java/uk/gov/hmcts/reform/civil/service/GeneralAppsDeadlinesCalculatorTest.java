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
        void shouldReturnDeadlineAs5WorkingDays_whenResponseDateIsWeekdayBefore4pm() {
            // Monday 9 Feb 2026 at 15:00 (3pm, before 4pm) starts from Mon 9 Feb
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
        void shouldReturnDeadlineAs5WorkingDays_whenResponseDateIsWeekdayAfter4pm() {
            // Monday 9 Feb 2026 at 18:00 (6pm, after 4pm) starts from Tue 10 Feb
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
        void shouldReturnDeadlineAs5WorkingDays_whenResponseDateIsFridayBeforeWeekend() {
            // Friday 13 Feb 2026 at 15:00 (3pm, before 4pm)
            // Result:  Thu 19
            // This gives 5 working days: 13, 16,17,18,19
            LocalDateTime fridayDate = LocalDate.of(2026, 2, 13).atTime(15, 0);
            LocalDateTime expectedDeadline = LocalDate.of(2026, 2, 19).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadlineWithWeekendCheck(fridayDate, 4);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }

        @Test
        void shouldReturnDeadlineAs5WorkingDays_from26MarchBefore4pm() {
            // Thursday 26 Mar 2026 at 15:00 (3pm, before 4pm) starts counting from 26 Mar
            // Result:  Wed 1 Apr 2026 at 4pm
            // This gives 5 working days: Thu 26, Fri 27, Mon 30, Tue 31, Wed 1 Apr
            LocalDateTime applicationDate = LocalDate.of(2026, 3, 26).atTime(15, 0);
            LocalDateTime expectedDeadline = LocalDate.of(2026, 4, 1).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadlineWithWeekendCheck(applicationDate, 4);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }

        @Test
        void shouldReturnDeadlineAs5WorkingDays_from26MarchAfter4pm() {
            // Thursday 26 Mar 2026 at 17:00 (5pm, after 4pm) starts counting from 27 Mar
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
        void shouldReturnDeadlineAs5WorkingDays_whenResultWouldFallOnSaturday() {
            // Tuesday 10 Feb 2026 at 15:00 (3pm, before 4pm) starts counting from 10 Feb
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
        void shouldReturnDeadlineAs5WorkingDays_whenResultWouldFallOnSunday() {
            // Wednesday 11 Feb 2026 at 15:00 (3pm, before 4pm) starts counting from 11 Feb
            // Result: Tue 17 Feb ( Sunday is excluded)
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
