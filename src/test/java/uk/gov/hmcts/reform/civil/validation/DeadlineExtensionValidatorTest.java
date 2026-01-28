package uk.gov.hmcts.reform.civil.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeadlineExtensionValidatorTest {

    private static final LocalDate NOW = now();

    @Mock
    private WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    private DeadlineExtensionValidator validator;

    @Nested
    class ValidateProposedDeadLine {

        @Test
        void shouldReturnNoErrors_whenValidExtension() {
            LocalDate agreedExtension = NOW.plusDays(14);
            LocalDateTime currentResponseDeadline = NOW.plusDays(7).atTime(16, 0);

            List<String> errors = validator.validateProposedDeadline(agreedExtension, currentResponseDeadline);

            assertThat(errors).isEmpty();
        }

        @Test
        void shouldReturnErrors_whenExtensionIsMoreThan28DaysFromResponseDeadline() {
            LocalDate agreedExtension = NOW.plusDays(29);
            LocalDateTime currentResponseDeadline = NOW.atTime(16, 0);

            List<String> errors = validator.validateProposedDeadline(agreedExtension, currentResponseDeadline);

            assertThat(errors)
                .containsOnly("The agreed extension date cannot be more than 28 days after the current deadline");
        }

        @Test
        void shouldReturnError_whenExtensionInPast() {
            LocalDate agreedExtension = NOW.minusDays(10);
            LocalDateTime currentResponseDeadline = NOW.atTime(16, 0);

            List<String> errors = validator.validateProposedDeadline(agreedExtension, currentResponseDeadline);

            assertThat(errors).containsOnly("The agreed extension date must be a date in the future");
        }

        @Test
        void shouldReturnError_whenExtensionIsSameAsResponseDeadline() {
            LocalDate agreedExtension = NOW.plusDays(5);
            LocalDateTime currentResponseDeadline = NOW.plusDays(5).atTime(16, 0);

            List<String> errors = validator.validateProposedDeadline(agreedExtension, currentResponseDeadline);

            assertThat(errors).containsOnly("The agreed extension date must be after the current deadline");
        }

        @Test
        void shouldReturnError_whenExtensionIsBeforeResponseDeadline() {
            LocalDate agreedExtension = NOW.plusDays(4);
            LocalDateTime currentResponseDeadline = NOW.plusDays(5).atTime(16, 0);

            List<String> errors = validator.validateProposedDeadline(agreedExtension, currentResponseDeadline);

            assertThat(errors).containsOnly("The agreed extension date must be after the current deadline");
        }

        // For Spec
        @Test
        void shouldReturnNoErrors_whenValidExtensionForSpec() {
            when(workingDayIndicator.isWorkingDay(any())).thenReturn(true);
            LocalDate agreedExtension = NOW.plusDays(14);
            LocalDateTime currentResponseDeadline = NOW.plusDays(7).atTime(16, 0);

            List<String> errors = validator.specValidateProposedDeadline(
                agreedExtension,
                currentResponseDeadline,
                false
            );

            assertThat(errors).isEmpty();
        }

        @Test
        void shouldReturnErrors_whenExtensionDateisPastDateForSpec() {
            LocalDate agreedExtension = NOW.minusDays(10);
            LocalDateTime currentResponseDeadline = NOW.plusDays(7).atTime(16, 0);

            List<String> errors = validator.specValidateProposedDeadline(
                agreedExtension,
                currentResponseDeadline,
                false
            );

            assertThat(errors).contains("The agreed extension date must be a date in the future");
        }

        @Test
        void shouldReturnErrors_whenExtensionDateisAfterResponseDeadlineDateForSpec() {
            LocalDate agreedExtension = NOW.plusDays(5);
            LocalDateTime currentResponseDeadline = NOW.plusDays(7).atTime(16, 0);

            List<String> errors = validator.specValidateProposedDeadline(
                agreedExtension,
                currentResponseDeadline,
                false
            );

            assertThat(errors).contains("The agreed extension date must be after the current deadline");
        }

        @Test
        void shouldReturnErrors_whenAgreedExtensionDateIsWeekendForSpec() {
            when(workingDayIndicator.isWorkingDay(any())).thenReturn(false);

            LocalDate agreedExtension = NOW.with(DayOfWeek.SUNDAY).plusDays(7);
            LocalDateTime currentResponseDeadline = NOW.plusDays(6).atTime(16, 0);
            when(workingDayIndicator.getNextWorkingDay(any()))
                .thenReturn(currentResponseDeadline.toLocalDate().plusDays(10));

            List<String> errors = validator.specValidateProposedDeadline(
                agreedExtension,
                currentResponseDeadline,
                false
            );

            assertThat(errors).contains("Date must be next working weekday");
        }

        @Test
        void shouldReturnErrors_whenExtensionIsMoreThan28DaysFromResponseDeadlineSpec() {
            LocalDate agreedExtension = NOW.plusDays(29);
            LocalDateTime currentResponseDeadline = NOW.atTime(16, 0);

            List<String> errors = validator.specValidateProposedDeadline(
                agreedExtension,
                currentResponseDeadline,
                false
            );

            assertThat(errors)
                .containsOnly("The agreed extension date cannot be more than 28 days after the current deadline");
        }
    }

    @Nested
    class CalculateDate {
        @Test
        void maxDateWhenAcknowledge() {
            LocalDate now = LocalDate.now();
            LocalDateTime ack = now.minusDays(5L).atTime(16, 0);
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);
            Assertions.assertEquals(
                now.plusDays(56),
                validator.getMaxDate(now, ack)
            );
        }

        @Test
        void maxDateWhenAcknowledge1Holiday() {
            LocalDate now = LocalDate.now();
            LocalDateTime ack = now.minusDays(5L).atTime(16, 0);
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(false, true);
            Assertions.assertEquals(
                now.plusDays(57),
                validator.getMaxDate(now, ack)
            );
        }

        @Test
        void maxDateWhenNoAcknowledge() {
            LocalDate now = LocalDate.now();
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);
            Assertions.assertEquals(
                now.plusDays(42),
                validator.getMaxDate(now, null)
            );
        }

        @Test
        void maxDateWhenNoAcknowledge1Holiday() {
            LocalDate now = LocalDate.now();
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(false, true);
            Assertions.assertEquals(
                now.plusDays(43),
                validator.getMaxDate(now, null)
            );
        }
    }
}
