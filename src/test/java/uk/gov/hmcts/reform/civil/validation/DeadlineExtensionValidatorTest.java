package uk.gov.hmcts.reform.civil.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    DeadlineExtensionValidator.class,
    JacksonAutoConfiguration.class
})
class DeadlineExtensionValidatorTest {

    private static final LocalDate NOW = now();

    @MockBean
    private WorkingDayIndicator workingDayIndicator;

    @Autowired
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
            when(workingDayIndicator.isWorkingDay(any())).thenReturn(true);
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
            when(workingDayIndicator.isWorkingDay(any())).thenReturn(true);
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
        void shouldReturnErrors_whenAgreedExtensionDateIsBeyond56DaysForSpec() {
            when(workingDayIndicator.isWorkingDay(any())).thenReturn(false);

            LocalDate agreedExtension = NOW.with(DayOfWeek.SUNDAY).plusDays(7);
            LocalDateTime currentResponseDeadline = NOW.plusDays(7).atTime(16, 0);
            when(workingDayIndicator.getNextWorkingDay(any())).thenReturn(currentResponseDeadline.toLocalDate());

            List<String> errors = validator.specValidateProposedDeadline(
                agreedExtension,
                currentResponseDeadline,
                false
            );

            assertThat(errors).contains("Date must be from claim issue date plus a maximum of 56 days.");
        }

        @Test
        void shouldReturnErrors_whenAgreedExtensionDateIsBeyond29And56DaysForSpec() {
            when(workingDayIndicator.isWorkingDay(any())).thenReturn(false);

            LocalDate agreedExtension = NOW.with(DayOfWeek.SUNDAY).plusDays(7);
            LocalDateTime currentResponseDeadline = NOW.plusDays(7).atTime(16, 0);
            when(workingDayIndicator.getNextWorkingDay(any())).thenReturn(currentResponseDeadline.toLocalDate());

            List<String> errors = validator.specValidateProposedDeadline(
                agreedExtension,
                currentResponseDeadline,
                true
            );
            assertThat(errors).contains("Date must be from claim issue date plus a maximum of between 29 and 56 days.");
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
        public void maxDateWhenAcknowledge() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime ack = now.minusDays(5L);
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);
            Assertions.assertEquals(
                now.plusDays(56).toLocalDate(),
                validator.getMaxDate(now, ack)
            );
        }

        @Test
        public void maxDateWhenAcknowledge1Holiday() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime ack = now.minusDays(5L);
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(false, true);
            Assertions.assertEquals(
                now.plusDays(57).toLocalDate(),
                validator.getMaxDate(now, ack)
            );
        }

        @Test
        public void maxDateWhenNoAcknowledge() {
            LocalDateTime now = LocalDateTime.now();
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(true);
            Assertions.assertEquals(
                now.plusDays(42).toLocalDate(),
                validator.getMaxDate(now, null)
            );
        }

        @Test
        public void maxDateWhenNoAcknowledge1Holiday() {
            LocalDateTime now = LocalDateTime.now();
            when(workingDayIndicator.isWorkingDay(any(LocalDate.class))).thenReturn(false, true);
            Assertions.assertEquals(
                now.plusDays(43).toLocalDate(),
                validator.getMaxDate(now, null)
            );
        }
    }
}
