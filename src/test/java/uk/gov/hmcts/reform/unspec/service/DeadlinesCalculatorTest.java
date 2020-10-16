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
import uk.gov.hmcts.reform.unspec.enums.ServiceMethodType;
import uk.gov.hmcts.reform.unspec.helpers.ResourceReader;
import uk.gov.hmcts.reform.unspec.service.bankholidays.BankHolidays;
import uk.gov.hmcts.reform.unspec.service.bankholidays.BankHolidaysApi;
import uk.gov.hmcts.reform.unspec.service.bankholidays.NonWorkingDaysCollection;
import uk.gov.hmcts.reform.unspec.service.bankholidays.PublicHolidaysCollection;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;
import static java.time.Month.JULY;
import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static java.time.Month.NOVEMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.assertion.DayAssert.assertThat;
import static uk.gov.hmcts.reform.unspec.enums.ServiceMethodType.EMAIL;

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

    @Nested
    class DeemedDateOfService {

        @ParameterizedTest
        @EnumSource(value = ServiceMethodType.class)
        void shouldReturnExpectedDays_whenTimeIsBefore4pm(ServiceMethodType methodType) {
            LocalDate dateOfService = LocalDate.of(2020, AUGUST, 3);

            LocalDate deemedDateOfService = calculator.calculateDeemedDateOfService(dateOfService, methodType);

            assertThat(deemedDateOfService).isTheSame(dateOfService.plusDays(methodType.getDays()));
        }

        @ParameterizedTest
        @EnumSource(value = ServiceMethodType.class, names = {"EMAIL", "FAX"})
        void shouldReturnPlusOneDays_whenTimeIsAfter4pm(ServiceMethodType methodType) {
            LocalDate dateTime = LocalDate.of(2000, 1, 1);

            assertThat(calculator.calculateDeemedDateOfService(dateTime.atTime(16, 0), methodType))
                .isEqualTo(dateTime.plusDays(1));
        }

        @ParameterizedTest
        @EnumSource(value = ServiceMethodType.class, names = {"POST", "DOCUMENT_EXCHANGE", "OTHER"})
        void shouldReturnPlusTwoDays_whenTimeIsAfter4pm(ServiceMethodType methodType) {
            LocalDate dateOfService = LocalDate.of(2020, AUGUST, 3);

            LocalDate deemedDateOfService = calculator.calculateDeemedDateOfService(dateOfService, methodType);

            assertThat(deemedDateOfService).isTheSame(dateOfService.plusDays(2));
        }

        @Test
        void shouldThrowNullPointerException_whenDateOfServiceIsNull() {
            LocalDate dateOfService = null;

            Exception exception = assertThrows(
                NullPointerException.class,
                () -> calculator.calculateDeemedDateOfService(dateOfService, EMAIL)
            );

            assertEquals("dateOfService is marked non-null but is null", exception.getMessage());
        }

        @Test
        void shouldThrowNullPointerException_whenServiceMethodIsNull() {
            LocalDate dateOfService = LocalDate.now();
            Exception exception = assertThrows(
                NullPointerException.class,
                () -> calculator.calculateDeemedDateOfService(dateOfService, null)
            );

            assertEquals("serviceMethod is marked non-null but is null", exception.getMessage());
        }
    }

    static class ResponseDeadLineArgumentsProvider implements ArgumentsProvider {

        private static final LocalDate PLUS_14_DAYS_AS_SATURDAY = LocalDate.of(2020, AUGUST, 1);
        private static final LocalDate PLUS_14_DAYS_AS_SUNDAY = LocalDate.of(2020, AUGUST, 2);
        private static final LocalDate PLUS_14_DAYS_AS_MONDAY = LocalDate.of(2020, AUGUST, 3);
        private static final LocalDateTime MONDAY_AS_DEADLINE = LocalDateTime.of(2020, AUGUST, 17, 16, 0);

        private static final LocalDate PLUS_14_DAYS_CHRISTMAS_DAY = LocalDate.of(2020, DECEMBER, 25).minusDays(14);
        private static final LocalDateTime NEXT_WORKING_DAY_AS_DEADLINE = LocalDateTime.of(2020, DECEMBER, 29, 16, 0);

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(PLUS_14_DAYS_AS_SATURDAY, MONDAY_AS_DEADLINE),
                Arguments.of(PLUS_14_DAYS_AS_SUNDAY, MONDAY_AS_DEADLINE),
                Arguments.of(PLUS_14_DAYS_AS_MONDAY, MONDAY_AS_DEADLINE),
                Arguments.of(PLUS_14_DAYS_CHRISTMAS_DAY, NEXT_WORKING_DAY_AS_DEADLINE)
            );
        }
    }

    @Nested
    class DefendantResponseDeadline {

        @ParameterizedTest(name = "{index} => should return responseDeadline {1} when deemedDateOfService {0}")
        @ArgumentsSource(ResponseDeadLineArgumentsProvider.class)
        void shouldReturnExpectedResponseDeadline_whenDeemedDateOfService(
            LocalDate deemedDateOfService,
            LocalDateTime expectedResponseDeadline
        ) {
            LocalDateTime responseDeadline = calculator.calculateDefendantResponseDeadline(deemedDateOfService);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedResponseDeadline);
        }

        @Test
        void shouldThrowNullPointerException_whenDeemedDateOfServiceIsNull() {
            Exception exception = assertThrows(
                NullPointerException.class,
                () -> calculator.calculateDefendantResponseDeadline(null)
            );

            assertEquals("deemedDateOfService is marked non-null but is null", exception.getMessage());
        }
    }

    static class ConfirmationOfServiceArgumentsProvider implements ArgumentsProvider {

        private static final LocalDate PLUS_4_MONTHS_AS_SATURDAY = LocalDate.of(2020, JANUARY, 2);
        private static final LocalDate PLUS_4_MONTHS_AS_SUNDAY = LocalDate.of(2020, JANUARY, 3);
        private static final LocalDate PLUS_4_MONTHS_AS_MONDAY = LocalDate.of(2020, JANUARY, 4);
        private static final LocalDateTime MONDAY_AS_DEADLINE = LocalDateTime.of(2020, MAY, 4, 23, 59, 59);

        private static final LocalDate PLUS_4_MONTHS_AS_CHRISTMAS_DAY = LocalDate.of(2020, DECEMBER, 25).minusMonths(4);
        private static final LocalDateTime NEXT_WORKING_DAY_AS_DEADLINE = LocalDateTime.of(2020, DECEMBER, 29, 16, 0);

        private static final LocalDate PLUS_4_MONTHS_AS_28_FEB_SUN = LocalDate.of(2021, FEBRUARY, 28).minusMonths(4);
        private static final LocalDateTime FIRST_WORKING_DAY_MONDAY = LocalDateTime.of(2021, MARCH, 1, 23, 59, 59);

        private static final LocalDate ISSUE_DATE_AS_31 = LocalDate.of(2020, JULY, 31);
        private static final LocalDateTime CONFIRMATION_OF_SERVICE_ON_30 = LocalDateTime.of(
            2020,
            NOVEMBER,
            30,
            23,
            59,
            59
        );

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(PLUS_4_MONTHS_AS_SATURDAY, MONDAY_AS_DEADLINE),
                Arguments.of(PLUS_4_MONTHS_AS_SUNDAY, MONDAY_AS_DEADLINE),
                Arguments.of(PLUS_4_MONTHS_AS_MONDAY, MONDAY_AS_DEADLINE),
                Arguments.of(PLUS_4_MONTHS_AS_CHRISTMAS_DAY, NEXT_WORKING_DAY_AS_DEADLINE),
                Arguments.of(PLUS_4_MONTHS_AS_28_FEB_SUN, FIRST_WORKING_DAY_MONDAY),
                Arguments.of(ISSUE_DATE_AS_31, CONFIRMATION_OF_SERVICE_ON_30)
            );
        }
    }

    @Nested
    class ConfirmationOfServiceDeadline {

        @ParameterizedTest
        @ArgumentsSource(ConfirmationOfServiceArgumentsProvider.class)
        void shouldReturnExpectedConfirmationOfServiceDeadline_whenIssueDate(
            LocalDate issueDate,
            LocalDateTime expectedConfirmationOfServiceDeadline
        ) {
            LocalDateTime responseDeadline = calculator.calculateConfirmationOfServiceDeadline(issueDate);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedConfirmationOfServiceDeadline);
        }

        @Test
        void shouldThrowNullPointerException_whenIssueDateIsNull() {
            Exception exception = assertThrows(
                NullPointerException.class,
                () -> calculator.calculateConfirmationOfServiceDeadline(null)
            );

            assertEquals("issueDate is marked non-null but is null", exception.getMessage());
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
