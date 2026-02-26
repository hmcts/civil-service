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
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.ConstraintValidatorContext;

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
            // Given
            UnavailableDate date = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.SINGLE_DATE)
                .setDate(LocalDate.now().plusMonths(4));
            // When  // Then
            assertTrue(validator.isValid(date, constraintValidatorContext));
        }

        @Test
        void shouldBeValidDate_whenIsExactlyThanOneYearInTheFuture() {
            // Given
            UnavailableDate date = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.SINGLE_DATE)
                .setDate(LocalDate.now().plusYears(1));
            // When  // Then
            assertTrue(validator.isValid(date, constraintValidatorContext));
        }

        @Test
        void shouldBeInvalidDate_whenIsOneDayMoreThanOneYearInTheFuture() {
            // Given
            UnavailableDate date = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.SINGLE_DATE)
                .setDate(LocalDate.now().plusYears(1).plusDays(1));
            // When  // Then
            assertFalse(validator.isValid(date, constraintValidatorContext));
        }

        @Test
        void shouldBeInvalidDate_whenIsMoreThanOneYearInTheFuture() {
            // Given
            UnavailableDate date = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.SINGLE_DATE)
                .setDate(LocalDate.now().plusYears(2));
            // When  // Then
            assertFalse(validator.isValid(date, constraintValidatorContext));
        }

        @Test
        void shouldBeInvalidDate_whenInThePast() {
            // Given
            UnavailableDate date = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.SINGLE_DATE)
                .setDate(LocalDate.now().minusYears(1));
            // When  // Then
            assertFalse(validator.isValid(date, constraintValidatorContext));
        }

        @Test
        void shouldBeValidDate_whenToday() {
            // Given
            UnavailableDate date = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.SINGLE_DATE)
                .setDate(LocalDate.now());
            // When  // Then
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
            // Given
            UnavailableDate unavailableDate = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.SINGLE_DATE)
                .setDate(LocalDate.now());
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            Hearing hearing = new Hearing()
                .setUnavailableDatesRequired(YES)
                .setUnavailableDates(unavailableDates);
            // When  // Then
            assertThat(validator.validate(hearing)).isEmpty();
        }

        @Test
        void shouldReturnError_whenMoreThanOneYearInFuture() {
            // Given
            UnavailableDate unavailableDate = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.SINGLE_DATE)
                .setDate(LocalDate.now().plusYears(5));
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            Hearing hearing = new Hearing()
                .setUnavailableDatesRequired(YES)
                .setUnavailableDates(unavailableDates);
            // When  // Then
            assertThat(validator.validate(hearing))
                .containsExactly("Dates must be within the next 12 months.");
        }

        @Test
        void shouldReturnError_whenFromDateIsMoreThanOneYearInFutureForDateRange() {
            // Given: UnavailableDate From date is more than one year in Future
            UnavailableDate unavailableDate = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.DATE_RANGE)
                .setFromDate(LocalDate.now().plusYears(5))
                .setToDate(LocalDate.now().plusMonths(2));
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            Hearing hearing = new Hearing()
                .setUnavailableDatesRequired(YES)
                .setUnavailableDates(unavailableDates);
            // When: Validate is called
            // Then: It should return validation error message
            assertThat(validator.validate(hearing))
                .containsExactly("Dates must be within the next 12 months.");
        }

        @Test
        void shouldReturnError_whenToDateIsMoreThanOneYearInFutureForDateRange() {
            // Given: UnavailableDate ToDate is more than one year in Future
            UnavailableDate unavailableDate = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.DATE_RANGE)
                .setFromDate(LocalDate.now().plusMonths(5))
                .setToDate(LocalDate.now().plusYears(4));
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            Hearing hearing = new Hearing()
                .setUnavailableDatesRequired(YES)
                .setUnavailableDates(unavailableDates);
            // When: Validate is called
            // Then: It should return validation error message
            assertThat(validator.validate(hearing))
                .containsExactly("Dates must be within the next 12 months.");
        }

        @Test
        void shouldReturnError_whenInPast() {
            // Given
            UnavailableDate unavailableDate = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.SINGLE_DATE)
                .setDate(LocalDate.now().minusDays(5));
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            Hearing hearing = new Hearing()
                .setUnavailableDatesRequired(YES)
                .setUnavailableDates(unavailableDates);
            // When  // Then
            assertThat(validator.validate(hearing))
                .containsExactly("Unavailable Date cannot be past date");
        }

        @Test
        void shouldReturnError_whenFromDateIsInPast() {
            // Given: UnavailableDate From date is in past
            UnavailableDate unavailableDate = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.DATE_RANGE)
                .setFromDate(LocalDate.now().minusDays(5))
                .setToDate(LocalDate.now().plusDays(2));
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            Hearing hearing = new Hearing()
                .setUnavailableDatesRequired(YES)
                .setUnavailableDates(unavailableDates);
            // When: Validate is called
            // Then: It should return validation error message
            assertThat(validator.validate(hearing))
                .containsExactly("Unavailable Date cannot be past date");
        }

        @Test
        void shouldReturnError_whenToDateIsInPast() {
            // Given: UnavailableDate From date is in past
            UnavailableDate unavailableDate = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.DATE_RANGE)
                .setToDate(LocalDate.now().minusDays(5))
                .setFromDate(LocalDate.now().plusMonths(2));
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            Hearing hearing = new Hearing()
                .setUnavailableDatesRequired(YES)
                .setUnavailableDates(unavailableDates);
            // When: Validate is called
            // Then: It should return validation error message
            assertThat(validator.validate(hearing))
                .containsExactly("Unavailable Date cannot be past date");
        }

        @Test
        void shouldReturnError_whenRequiredButNullDates() {
            // Given
            Hearing hearing = new Hearing()
                .setUnavailableDatesRequired(YES);
            // When  // Then
            assertThat(validator.validate(hearing)).containsExactly("Details of unavailable date required");
        }

        @Test
        void shouldReturnError_whenRequiredButEmptyDates() {
            // Given
            Hearing hearing = new Hearing()
                .setUnavailableDatesRequired(YES)
                .setUnavailableDates(emptyList());
            // When  // Then
            assertThat(validator.validate(hearing)).containsExactly("Details of unavailable date required");
        }

        @Test
        void shouldReturnNoError_whenNotRequiredAndNoDates() {
            // Given
            Hearing hearing = new Hearing()
                .setUnavailableDatesRequired(NO);
            // When  // Then
            assertThat(validator.validate(hearing)).isEmpty();
        }

        @Test
        void shouldReturnError_whenHearingLRSpecValidatesDatesAndFastClaimIsNotNull() {
            // Given
            UnavailableDate unavailableDate = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.SINGLE_DATE)
                .setDate(LocalDate.now().minusDays(5));
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            Hearing hearing = new Hearing()
                .setUnavailableDatesRequired(YES)
                .setUnavailableDates(unavailableDates);
            // When  // Then
            assertThat(validator.validateFastClaimHearing(hearing))
                .containsExactly("Unavailable Date cannot be past date");
        }

        @Test
        void shouldReturnError_whenHearingLRSpecValidatesDatesAndFastClaimIsNull() {
            // Given
            Hearing hearing = new Hearing()
                .setUnavailableDatesRequired(YES);
            // When  // Then
            assertThat(validator.validateFastClaimHearing(hearing))
                .containsExactly("Details of unavailable date required");
        }

        @Test
        void shouldReturnEmptyList_whenHearingLRSpecIsNull() {
            // Given null hearing object
            // When: Validate is called
            // Then: It should not return validation error message
            assertThat(validator.validateFastClaimHearing(null))
                .isEmpty();
        }

        @Test
        void shouldReturnEmptyList_whenHearingNoUnAvailableDates() {
            // Given: Hearing with No Unavailable Dates
            Hearing hearing = new Hearing()
                .setUnavailableDatesRequired(NO);
            // When: Validate is called
            // Then: It should not return validation error message
            assertThat(validator.validateFastClaimHearing(hearing))
                .isEmpty();
        }

        @Test
        void shouldReturnError_whenSmallClaimHearingValidatesDatesAndSmallClaimHearingIsNull() {
            // Given
            SmallClaimHearing smallClaimHearing = new SmallClaimHearing()
                .setUnavailableDatesRequired(YES);
            // When  // Then
            assertThat(validator.validateSmallClaimsHearing(smallClaimHearing))
                .containsExactly("Details of unavailable date required");
        }

        @Test
        void shouldReturnError_whenSmallClaimHearingValidatesDatesAndSmallClaimHearingIsNotNull() {
            // Given
            UnavailableDate unavailableDate = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.SINGLE_DATE)
                .setDate(LocalDate.now().minusDays(5));
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            SmallClaimHearing hearing = new SmallClaimHearing()
                .setUnavailableDatesRequired(YES)
                .setSmallClaimUnavailableDate(unavailableDates);
            // When  // Then
            assertThat(validator.validateSmallClaimsHearing(hearing))
                .containsExactly("Unavailable Date cannot be past date");
        }

        @Test
        void shouldReturnError_whenSmallClaimHearingValidatesDatesAndDateInUnavaiableDateIsNull() {
            // Given
            UnavailableDate unavailableDate = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            SmallClaimHearing hearing = new SmallClaimHearing()
                .setUnavailableDatesRequired(YES)
                .setSmallClaimUnavailableDate(unavailableDates);
            // When  // Then
            assertThat(validator.validateSmallClaimsHearing(hearing))
                .containsExactly("Details of unavailable date required");
        }

        @Test
        void shouldReturnError_whenSmallClaimHearingValidatesDatesAndDateRangeInUnavaiableDateIsNull() {
            // Given
            UnavailableDate unavailableDate = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.DATE_RANGE);
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            SmallClaimHearing hearing = new SmallClaimHearing()
                .setUnavailableDatesRequired(YES)
                .setSmallClaimUnavailableDate(unavailableDates);
            // When  // Then
            assertThat(validator.validateSmallClaimsHearing(hearing))
                .containsExactly("Details of unavailable date required");
        }

        @Test
        void shouldReturnError_whenSmallClaimHearingValidatesDatesAndDateRangeInUnavaiableDateIsNotValid() {
            // Given
            UnavailableDate unavailableDate = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.DATE_RANGE)
                .setFromDate(LocalDate.now().plusDays(5))
                .setToDate(LocalDate.now().plusDays(4));
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            SmallClaimHearing hearing = new SmallClaimHearing()
                .setUnavailableDatesRequired(YES)
                .setSmallClaimUnavailableDate(unavailableDates);
            // When  // Then
            assertThat(validator.validateSmallClaimsHearing(hearing))
                .containsExactly("From Date should be less than To Date");
        }

        @Test
        void shouldReturnNoError_whenSmallClaimHearingValidatesDatesAndFromDateIsLessThanToDate() {
            // Given: UnAvailableDate Where FromDate is less than ToDate
            UnavailableDate unavailableDate = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.DATE_RANGE)
                .setFromDate(LocalDate.now().plusDays(4))
                .setToDate(LocalDate.now().plusDays(5));
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            SmallClaimHearing hearing = new SmallClaimHearing()
                .setUnavailableDatesRequired(YES)
                .setSmallClaimUnavailableDate(unavailableDates);
            // When: Validate is called
            // Then: It should not return any errors
            assertThat(validator.validateSmallClaimsHearing(hearing))
                .isEmpty();
        }

        @Test
        void shouldReturnError_whenSmallClaimHearingValidatesDatesAndDateRangeInUnavaiableToDateIsNull() {
            // Given: SmallClaimHearing object with ToDate is null
            UnavailableDate unavailableDate = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.DATE_RANGE)
                .setFromDate(LocalDate.now().plusDays(5));
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            SmallClaimHearing hearing = new SmallClaimHearing()
                .setUnavailableDatesRequired(YES)
                .setSmallClaimUnavailableDate(unavailableDates);
            // When: Validate is called
            // Then: It should return validation error message
            assertThat(validator.validateSmallClaimsHearing(hearing))
                .containsExactly("Details of unavailable date required");
        }

        @Test
        void shouldReturnError_whenSmallClaimHearingFromDateIsNull() {
            // Given: SmallClaimHearing object with FromDate is null
            UnavailableDate unavailableDate = new UnavailableDate()
                .setUnavailableDateType(UnavailableDateType.DATE_RANGE)
                .setToDate(LocalDate.now().plusDays(4));
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            SmallClaimHearing hearing = new SmallClaimHearing()
                .setUnavailableDatesRequired(YES)
                .setSmallClaimUnavailableDate(unavailableDates);
            // When: Validate is called
            // Then: It should return validation error message
            assertThat(validator.validateSmallClaimsHearing(hearing))
                .containsExactly("Details of unavailable date required");
        }

        @Test
        void shouldReturnEmptyErrorList_whenNoUnavailableDates() {
            // Given: SmallClaimHearing with No Unavailable Dates
            SmallClaimHearing hearing = new SmallClaimHearing()
                .setUnavailableDatesRequired(NO);
            // When: Validate is called
            // Then: It should not return validation error message
            assertThat(validator.validateSmallClaimsHearing(hearing))
                .isEmpty();
        }
    }

    @Nested
    class RepaymentPlanTest {

        @InjectMocks
        UnavailableDateValidator validator;

        @Test
        void shouldReturnError_whenPaymentDateMoreThanOneYearInFuture() {
            // When  // Then
            assertThat(validator.validateFuturePaymentDate(LocalDate.now().plusDays(368)))
                .containsExactly("Date of first payment must be today or within the next 12 months");
        }

        @Test
        void shouldReturnError_whenPaymentDateOfPast() {
            // When  // Then
            assertThat(validator.validateFuturePaymentDate(LocalDate.now().minusDays(12)))
                .containsExactly("Date of first payment must be today or within the next 12 months");
        }

        @Test
        void shouldNotReturnError_whenPaymentDateIsToday() {
            assertThat(validator.validateFuturePaymentDate(LocalDate.now())).isEmpty();
        }
    }
}
