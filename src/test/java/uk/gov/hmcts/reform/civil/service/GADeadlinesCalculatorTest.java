package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.lang.Assert;
import org.assertj.core.api.AbstractAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.assertion.DayAssert;
import uk.gov.hmcts.reform.civil.helpers.ResourceReader;
import uk.gov.hmcts.reform.civil.service.bankholidays.BankHolidays;
import uk.gov.hmcts.reform.civil.service.bankholidays.BankHolidaysApi;
import uk.gov.hmcts.reform.civil.service.bankholidays.NonWorkingDaysCollection;
import uk.gov.hmcts.reform.civil.service.bankholidays.PublicHolidaysCollection;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.assertion.DayAssert.assertThat;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;

@ExtendWith(SpringExtension.class)
public class GADeadlinesCalculatorTest {

    @Mock
    private BankHolidaysApi bankHolidaysApi;
    @Mock
    private NonWorkingDaysCollection nonWorkingDaysCollection;

    private GADeadlinesCalculator calculator;

    @BeforeEach
    public void setUp() throws IOException {
        WorkingDayIndicator workingDayIndicator = new WorkingDayIndicator(
            new PublicHolidaysCollection(bankHolidaysApi),
            nonWorkingDaysCollection
        );
        when(bankHolidaysApi.retrieveAll()).thenReturn(loadFixture());

        calculator = new GADeadlinesCalculator(workingDayIndicator);
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
            LocalDateTime weekdayDate = LocalDate.of(2022, 2, 15).atTime(12,0);
            LocalDateTime expectedDeadline = weekdayDate.toLocalDate().plusDays(2).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadline(weekdayDate,2);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }

        @Test
        void shouldReturnDeadlinePlus3Days_whenResponseDateIsWeekday() {
            LocalDateTime weekdayDate = LocalDate.of(2022, 2, 15).atTime(12,0);
            LocalDateTime expectedDeadline = weekdayDate.toLocalDate().plusDays(3).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadline(weekdayDate,3);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }

    }

}
