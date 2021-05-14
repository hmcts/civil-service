package uk.gov.hmcts.reform.unspec.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.unspec.enums.AllocatedTrack;
import uk.gov.hmcts.reform.unspec.helpers.ResourceReader;
import uk.gov.hmcts.reform.unspec.service.bankholidays.BankHolidays;
import uk.gov.hmcts.reform.unspec.service.bankholidays.BankHolidaysApi;
import uk.gov.hmcts.reform.unspec.service.bankholidays.NonWorkingDaysCollection;
import uk.gov.hmcts.reform.unspec.service.bankholidays.PublicHolidaysCollection;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static java.time.LocalTime.MIDNIGHT;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JULY;
import static java.time.Month.JUNE;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.assertion.DayAssert.assertThat;
import static uk.gov.hmcts.reform.unspec.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;

@ExtendWith(SpringExtension.class)
public class DeadlinesCalculatorTest {

    @Mock
    private BankHolidaysApi bankHolidaysApi;
    @Mock
    private NonWorkingDaysCollection nonWorkingDaysCollection;

    private DeadlinesCalculator calculator;

    @BeforeEach
    public void setUp() throws IOException {
        WorkingDayIndicator workingDayIndicator = new WorkingDayIndicator(
            new PublicHolidaysCollection(bankHolidaysApi),
            nonWorkingDaysCollection
        );

        when(bankHolidaysApi.retrieveAll()).thenReturn(loadFixture());

        calculator = new DeadlinesCalculator(workingDayIndicator);
    }

    static class ClaimNotificationDeadlineArgumentsProvider implements ArgumentsProvider {

        private static final LocalDate SATURDAY_DATE = LocalDate.of(2020, AUGUST, 5);
        private static final LocalDate SUNDAY_DATE = LocalDate.of(2020, AUGUST, 6);
        private static final LocalDate MONDAY_DATE = LocalDate.of(2020, AUGUST, 7);
        private static final LocalDateTime EXPECTED_DEADLINE = LocalDate.of(2020, DECEMBER, 7).atTime(MIDNIGHT);
        private static final LocalDate CHRISTMAS_DAY = LocalDate.of(2020, DECEMBER, 25).minusMonths(4);
        private static final LocalDateTime EXPECTED_CHRISTMAS = LocalDate.of(2020, DECEMBER, 29).atTime(MIDNIGHT);
        private static final LocalDate AUGUST_25_2017 = LocalDate.of(2017, AUGUST, 25);
        private static final LocalDateTime CHRISTMAS_27TH_WEDNESDAY = LocalDate.of(2017, DECEMBER, 27)
            .atTime(MIDNIGHT);
        private static final LocalDate DATE_31ST = LocalDate.of(2020, JULY, 31);
        private static final LocalDateTime EXPECTED_DATE_30TH = LocalDate.of(2020, NOVEMBER, 30).atTime(MIDNIGHT);
        private static final LocalDate OCTOBER_DATE_2018 = LocalDate.of(2018, OCTOBER, 30);
        private static final LocalDateTime FEBRUARY_28_2019_NON_LEAP_YEAR = LocalDate.of(2019, FEBRUARY, 28)
            .atTime(MIDNIGHT);
        private static final LocalDate OCTOBER_DATE_2015 = LocalDate.of(2015, OCTOBER, 30);
        private static final LocalDateTime FEBRUARY_29_2016_LEAP_YEAR = LocalDate.of(2016, FEBRUARY, 29)
            .atTime(MIDNIGHT);

        private static final LocalDate FEBRUARY_28TH = LocalDate.of(2018, FEBRUARY, 28);
        private static final LocalDateTime JUNE_28TH = LocalDate.of(2018, JUNE, 28).atTime(MIDNIGHT);

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(SATURDAY_DATE, EXPECTED_DEADLINE),
                Arguments.of(SUNDAY_DATE, EXPECTED_DEADLINE),
                Arguments.of(MONDAY_DATE, EXPECTED_DEADLINE),
                Arguments.of(CHRISTMAS_DAY, EXPECTED_CHRISTMAS),
                Arguments.of(AUGUST_25_2017, CHRISTMAS_27TH_WEDNESDAY),
                Arguments.of(DATE_31ST, EXPECTED_DATE_30TH),
                Arguments.of(OCTOBER_DATE_2018, FEBRUARY_28_2019_NON_LEAP_YEAR),
                Arguments.of(OCTOBER_DATE_2015, FEBRUARY_29_2016_LEAP_YEAR),
                Arguments.of(FEBRUARY_28TH, JUNE_28TH)
            );
        }
    }

    @Nested
    class AddMonthsToDateToNextWorkingDayAtMidnight {

        @ParameterizedTest(name = "{index} => should return responseDeadline {1} when issueDate {0}")
        @ArgumentsSource(ClaimNotificationDeadlineArgumentsProvider.class)
        void shouldReturnExpectedClaimNotificationDate_whenGivenIssueDate(
            LocalDate issueDate,
            LocalDateTime expectedResponseDeadline
        ) {
            LocalDateTime responseDeadline = calculator.addMonthsToDateToNextWorkingDayAtMidnight(4, issueDate);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedResponseDeadline);
        }
    }

    @Nested
    class AddMonthsToDateAtMidnight {

        @Test
        void shouldReturnDatePlus4Months_whenWeekday() {
            LocalDate weekdayDate = LocalDate.of(2021, 2, 4);
            LocalDateTime expectedDeadline = LocalDate.of(2021, 6, 4).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.addMonthsToDateAtMidnight(4, weekdayDate);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }

        @Test
        void shouldReturnDatePlus4Months_whenWeekend() {
            LocalDate weekendDate = LocalDate.of(2021, 2, 6);
            LocalDateTime expectedDeadline = LocalDate.of(2021, 6, 6).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.addMonthsToDateAtMidnight(4, weekendDate);

            assertThat(responseDeadline)
                .isTheSame(expectedDeadline);
        }
    }

    static class ClaimDetailsNotificationDeadlineArgumentsProvider implements ArgumentsProvider {

        private static final LocalDateTime SATURDAY = LocalDate.of(2020, AUGUST, 1).atTime(12, 0);
        private static final LocalDateTime SATURDAY_AFTER_4PM = LocalDate.of(2020, AUGUST, 1).atTime(17, 0);
        private static final LocalDateTime SATURDAY_AT_4PM = LocalDate.of(2020, AUGUST, 1).atTime(16, 0);
        private static final LocalDateTime SUNDAY = LocalDate.of(2020, AUGUST, 2).atTime(12, 0);
        private static final LocalDateTime MONDAY = LocalDate.of(2020, AUGUST, 3).atTime(12, 0);
        private static final LocalDateTime MONDAY_AFTER_4PM = LocalDate.of(2020, AUGUST, 3).atTime(17, 0);
        private static final LocalDateTime MONDAY_AT_4PM = LocalDate.of(2020, AUGUST, 3).atTime(16, 0);
        private static final LocalDateTime MONDAY_AS_DEADLINE = LocalDateTime.of(2020, AUGUST, 17, 16, 0);

        private static final LocalDateTime CHRISTMAS_DAY = LocalDate.of(2020, DECEMBER, 25).minusDays(14)
            .atTime(12, 0);
        private static final LocalDateTime CHRISTMAS_DAY_AT_4PM = LocalDate.of(2020, DECEMBER, 25).minusDays(14)
            .atTime(16, 0);
        private static final LocalDateTime CHRISTMAS_DAY_AFTER_4PM = LocalDate.of(2020, DECEMBER, 25).minusDays(14)
            .atTime(17, 0);
        private static final LocalDateTime NEXT_WORKING_DAY = LocalDateTime.of(2020, DECEMBER, 29, 16, 0);

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(SATURDAY, MONDAY_AS_DEADLINE),
                Arguments.of(SATURDAY_AFTER_4PM, MONDAY_AS_DEADLINE),
                Arguments.of(SATURDAY_AT_4PM, MONDAY_AS_DEADLINE),
                Arguments.of(SUNDAY, MONDAY_AS_DEADLINE),
                Arguments.of(MONDAY, MONDAY_AS_DEADLINE),
                Arguments.of(CHRISTMAS_DAY, NEXT_WORKING_DAY),
                Arguments.of(CHRISTMAS_DAY_AT_4PM, NEXT_WORKING_DAY),
                Arguments.of(CHRISTMAS_DAY_AFTER_4PM, NEXT_WORKING_DAY),
                Arguments.of(MONDAY_AFTER_4PM, MONDAY_AS_DEADLINE.plusDays(1)),
                Arguments.of(MONDAY_AT_4PM, MONDAY_AS_DEADLINE.plusDays(1))
            );
        }
    }

    @Nested
    class ClaimDetailsNotificationDeadline {

        @ParameterizedTest(name = "{index} => should return responseDeadline {1} when issueDate {0}")
        @ArgumentsSource(ClaimDetailsNotificationDeadlineArgumentsProvider.class)
        void shouldReturnExpectedResponseDeadline_whenClaimIssueDate(
            LocalDateTime claimIssueDate,
            LocalDateTime expectedResponseDeadline
        ) {
            LocalDateTime responseDeadline = calculator.plus14DaysAt4pmDeadline(claimIssueDate);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedResponseDeadline);
        }
    }

    @Nested
    class ApplicantResponseDeadline {

        @Test
        void shouldReturnDeadlinePlus14Days_whenResponseDateIsWeekdayAndTrackIsSmallClaim() {
            LocalDateTime weekdayDate = LocalDate.of(2021, 2, 4).atTime(12, 0);
            LocalDateTime expectedDeadline = weekdayDate.toLocalDate().plusDays(14).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadline(weekdayDate, SMALL_CLAIM);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }

        @Test
        void shouldReturnDeadlinePlus14Days_whenResponseDateIsWeekendAndTrackIsSmallClaim() {
            LocalDateTime weekendDate = LocalDate.of(2021, 2, 6).atTime(12, 0);
            LocalDateTime expectedDeadline = LocalDate.of(2021, 2, 22).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadline(weekendDate, SMALL_CLAIM);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }

        @Test
        void shouldReturnDeadlinePlus14DaysWithAnExtraDay_whenResponseDateIsWeekdayAfter4pmAndTrackIsSmallClaim() {
            LocalDateTime weekdayDate = LocalDate.of(2021, 2, 4).atTime(17, 0);
            LocalDateTime expectedDeadline = weekdayDate.toLocalDate().plusDays(14).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadline(weekdayDate, SMALL_CLAIM);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline.plusDays(1));
        }

        @Test
        void shouldReturnDeadlinePlus14DaysWithAnExtraDay_whenResponseDateIsWeekdayAt4pmAndTrackIsSmallClaim() {
            LocalDateTime weekdayDate = LocalDate.of(2021, 2, 4).atTime(16, 0);
            LocalDateTime expectedDeadline = weekdayDate.toLocalDate().plusDays(14).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadline(weekdayDate, SMALL_CLAIM);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline.plusDays(1));
        }

        @ParameterizedTest
        @EnumSource(value = AllocatedTrack.class, mode = EnumSource.Mode.EXCLUDE, names = "SMALL_CLAIM")
        void shouldReturnDeadlinePlus28Days_whenResponseDateIsWeekdayAndTrackIsNotSmallClaim(AllocatedTrack track) {
            LocalDateTime weekdayDate = LocalDate.of(2021, 2, 4).atTime(12, 0);
            LocalDateTime expectedDeadline = weekdayDate.toLocalDate().plusDays(28).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadline(weekdayDate, track);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }

        @ParameterizedTest
        @EnumSource(value = AllocatedTrack.class, mode = EnumSource.Mode.EXCLUDE, names = "SMALL_CLAIM")
        void shouldReturnDeadlinePlus28Days_whenResponseDateIsWeekendAndTrackIsNotSmallClaim(AllocatedTrack track) {
            LocalDateTime weekendDate = LocalDate.of(2021, 2, 6).atTime(12, 0);
            LocalDateTime expectedDeadline = LocalDate.of(2021, 3, 8).atTime(END_OF_BUSINESS_DAY);
            LocalDateTime responseDeadline = calculator.calculateApplicantResponseDeadline(weekendDate, track);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedDeadline);
        }
    }

    /**
     * The fixture is taken from the real bank holidays API.
     */
    private BankHolidays loadFixture() throws IOException {
        String input = ResourceReader.readString("/bank-holidays.json");
        return new ObjectMapper().readValue(input, BankHolidays.class);
    }
}
