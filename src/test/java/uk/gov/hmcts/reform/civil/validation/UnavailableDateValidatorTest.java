package uk.gov.hmcts.reform.civil.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;

import java.time.LocalDate;
import java.util.List;
import javax.validation.ConstraintValidatorContext;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class UnavailableDateValidatorTest {

    @Nested
    class IsValid {

        @Mock
        ConstraintValidatorContext constraintValidatorContext;

        @InjectMocks
        UnavailableDateValidator validator;

        @Test
        void shouldBeValidDate_whenLessThanOneYearInTheFuture() {
            UnavailableDate date = UnavailableDate.builder()
                .date(LocalDate.now().plusMonths(4))
                .build();

            assertTrue(validator.isValid(date, constraintValidatorContext));
        }

        @Test
        void shouldBeValidDate_whenIsExactlyThanOneYearInTheFuture() {
            UnavailableDate date = UnavailableDate.builder()
                .date(LocalDate.now().plusYears(1))
                .build();

            assertTrue(validator.isValid(date, constraintValidatorContext));
        }

        @Test
        void shouldBeInvalidDate_whenIsOneDayMoreThanOneYearInTheFuture() {
            UnavailableDate date = UnavailableDate.builder()
                .date(LocalDate.now().plusYears(1).plusDays(1))
                .build();

            assertFalse(validator.isValid(date, constraintValidatorContext));
        }

        @Test
        void shouldBeInvalidDate_whenIsMoreThanOneYearInTheFuture() {
            UnavailableDate date = UnavailableDate.builder()
                .date(LocalDate.now().plusYears(2))
                .build();

            assertFalse(validator.isValid(date, constraintValidatorContext));
        }

        @Test
        void shouldBeInvalidDate_whenInThePast() {
            UnavailableDate date = UnavailableDate.builder()
                .date(LocalDate.now().minusYears(1))
                .build();

            assertFalse(validator.isValid(date, constraintValidatorContext));
        }

        @Test
        void shouldBeValidDate_whenToday() {
            UnavailableDate date = UnavailableDate.builder()
                .date(LocalDate.now())
                .build();

            assertTrue(validator.isValid(date, constraintValidatorContext));
        }
    }

    @Nested
    @SpringBootTest(classes = {UnavailableDateValidator.class, ValidationAutoConfiguration.class})
    class Validator {

        @Autowired
        UnavailableDateValidator validator;

        @Test
        void shouldReturnNoError_whenToday() {
            UnavailableDate unavailableDate = UnavailableDate.builder().date(LocalDate.now()).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);

            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(unavailableDates)
                .build();

            assertThat(validator.validate(hearing)).isEmpty();
        }

        @Test
        void shouldReturnError_whenMoreThanOneYearInFuture() {
            UnavailableDate unavailableDate = UnavailableDate.builder().date(LocalDate.now().plusYears(5)).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);

            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(unavailableDates)
                .build();

            assertThat(validator.validate(hearing))
                .containsExactly("The date cannot be in the past and must not be more than a year in the future");
        }

        @Test
        void shouldReturnError_whenInPast() {
            UnavailableDate unavailableDate = UnavailableDate.builder().date(LocalDate.now().minusDays(5)).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);

            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(unavailableDates)
                .build();

            assertThat(validator.validate(hearing))
                .containsExactly("The date cannot be in the past and must not be more than a year in the future");
        }

        @Test
        void shouldReturnError_whenRequiredButNullDates() {
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .build();

            assertThat(validator.validate(hearing)).containsExactly("Details of unavailable date required");
        }

        @Test
        void shouldReturnError_whenRequiredButEmptyDates() {
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(emptyList())
                .build();

            assertThat(validator.validate(hearing)).containsExactly("Details of unavailable date required");
        }

        @Test
        void shouldReturnNoError_whenNotRequiredAndNoDates() {
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(NO)
                .build();

            assertThat(validator.validate(hearing)).isEmpty();
        }
    }
}
