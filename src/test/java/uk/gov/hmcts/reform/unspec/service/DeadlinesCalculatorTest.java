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
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.assertion.DayAssert.assertThat;

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
    class RespondentResponseDeadline {

        @ParameterizedTest(name = "{index} => should return responseDeadline {1} when claimIssueDate {0}")
        @ArgumentsSource(ResponseDeadLineArgumentsProvider.class)
        void shouldReturnExpectedResponseDeadline_whenClaimIssueDate(
            LocalDate claimIssueDate,
            LocalDateTime expectedResponseDeadline
        ) {
            LocalDateTime responseDeadline = calculator.calculateResponseDeadline(claimIssueDate);

            assertThat(responseDeadline)
                .isWeekday()
                .isTheSame(expectedResponseDeadline);
        }

        @Test
        void shouldThrowNullPointerException_whenClaimIssueDateIsNull() {
            Exception exception = assertThrows(
                NullPointerException.class,
                () -> calculator.calculateResponseDeadline(null)
            );

            assertEquals("claimIssueDate is marked non-null but is null", exception.getMessage());
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
