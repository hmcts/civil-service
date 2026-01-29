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
            // Given
            UnavailableDate date = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.now().plusMonths(4))
                .build();
            // When  // Then
            assertTrue(validator.isValid(date, constraintValidatorContext));
        }

        @Test
        void shouldBeValidDate_whenIsExactlyThanOneYearInTheFuture() {
            // Given
            UnavailableDate date = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.now().plusYears(1))
                .build();
            // When  // Then
            assertTrue(validator.isValid(date, constraintValidatorContext));
        }

        @Test
        void shouldBeInvalidDate_whenIsOneDayMoreThanOneYearInTheFuture() {
            // Given
            UnavailableDate date = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.now().plusYears(1).plusDays(1))
                .build();
            // When  // Then
            assertFalse(validator.isValid(date, constraintValidatorContext));
        }

        @Test
        void shouldBeInvalidDate_whenIsMoreThanOneYearInTheFuture() {
            // Given
            UnavailableDate date = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.now().plusYears(2))
                .build();
            // When  // Then
            assertFalse(validator.isValid(date, constraintValidatorContext));
        }

        @Test
        void shouldBeInvalidDate_whenInThePast() {
            // Given
            UnavailableDate date = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.now().minusYears(1))
                .build();
            // When  // Then
            assertFalse(validator.isValid(date, constraintValidatorContext));
        }

        @Test
        void shouldBeValidDate_whenToday() {
            // Given
            UnavailableDate date = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.now())
                .build();
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
            UnavailableDate unavailableDate = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.now()).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(unavailableDates)
                .build();
            // When  // Then
            assertThat(validator.validate(hearing)).isEmpty();
        }

        @Test
        void shouldReturnError_whenMoreThanOneYearInFuture() {
            // Given
            UnavailableDate unavailableDate = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.now().plusYears(5)).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(unavailableDates)
                .build();
            // When  // Then
            assertThat(validator.validate(hearing))
                .containsExactly("Dates must be within the next 12 months.");
        }

        @Test
        void shouldReturnError_whenFromDateIsMoreThanOneYearInFutureForDateRange() {
            // Given: UnavailableDate From date is more than one year in Future
            UnavailableDate unavailableDate = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                .fromDate(LocalDate.now().plusYears(5))
                .toDate(LocalDate.now().plusMonths(2)).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(unavailableDates)
                .build();
            // When: Validate is called
            // Then: It should return validation error message
            assertThat(validator.validate(hearing))
                .containsExactly("Dates must be within the next 12 months.");
        }

        @Test
        void shouldReturnError_whenToDateIsMoreThanOneYearInFutureForDateRange() {
            // Given: UnavailableDate ToDate is more than one year in Future
            UnavailableDate unavailableDate = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                .fromDate(LocalDate.now().plusMonths(5))
                .toDate(LocalDate.now().plusYears(4)).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(unavailableDates)
                .build();
            // When: Validate is called
            // Then: It should return validation error message
            assertThat(validator.validate(hearing))
                .containsExactly("Dates must be within the next 12 months.");
        }

        @Test
        void shouldReturnError_whenInPast() {
            // Given
            UnavailableDate unavailableDate = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.now().minusDays(5)).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(unavailableDates)
                .build();
            // When  // Then
            assertThat(validator.validate(hearing))
                .containsExactly("Unavailable Date cannot be past date");
        }

        @Test
        void shouldReturnError_whenFromDateIsInPast() {
            // Given: UnavailableDate From date is in past
            UnavailableDate unavailableDate = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                .fromDate(LocalDate.now().minusDays(5))
                .toDate(LocalDate.now().plusDays(2)).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(unavailableDates)
                .build();
            // When: Validate is called
            // Then: It should return validation error message
            assertThat(validator.validate(hearing))
                .containsExactly("Unavailable Date cannot be past date");
        }

        @Test
        void shouldReturnError_whenToDateIsInPast() {
            // Given: UnavailableDate From date is in past
            UnavailableDate unavailableDate = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                .toDate(LocalDate.now().minusDays(5))
                .fromDate(LocalDate.now().plusMonths(2)).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(unavailableDates)
                .build();
            // When: Validate is called
            // Then: It should return validation error message
            assertThat(validator.validate(hearing))
                .containsExactly("Unavailable Date cannot be past date");
        }

        @Test
        void shouldReturnError_whenRequiredButNullDates() {
            // Given
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .build();
            // When  // Then
            assertThat(validator.validate(hearing)).containsExactly("Details of unavailable date required");
        }

        @Test
        void shouldReturnError_whenRequiredButEmptyDates() {
            // Given
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(emptyList())
                .build();
            // When  // Then
            assertThat(validator.validate(hearing)).containsExactly("Details of unavailable date required");
        }

        @Test
        void shouldReturnNoError_whenNotRequiredAndNoDates() {
            // Given
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(NO)
                .build();
            // When  // Then
            assertThat(validator.validate(hearing)).isEmpty();
        }

        @Test
        void shouldReturnError_whenHearingLRSpecValidatesDatesAndFastClaimIsNotNull() {
            // Given
            UnavailableDate unavailableDate = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.now().minusDays(5)).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .unavailableDates(unavailableDates)
                .build();
            // When  // Then
            assertThat(validator.validateFastClaimHearing(hearing))
                .containsExactly("Unavailable Date cannot be past date");
        }

        @Test
        void shouldReturnError_whenHearingLRSpecValidatesDatesAndFastClaimIsNull() {
            // Given
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(YES)
                .build();
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
            Hearing hearing = Hearing.builder()
                .unavailableDatesRequired(NO)
                .build();
            // When: Validate is called
            // Then: It should not return validation error message
            assertThat(validator.validateFastClaimHearing(hearing))
                .isEmpty();
        }

        @Test
        void shouldReturnError_whenSmallClaimHearingValidatesDatesAndSmallClaimHearingIsNull() {
            // Given
            SmallClaimHearing smallClaimHearing = SmallClaimHearing.builder()
                .unavailableDatesRequired(YES).build();
            // When  // Then
            assertThat(validator.validateSmallClaimsHearing(smallClaimHearing))
                .containsExactly("Details of unavailable date required");
        }

        @Test
        void shouldReturnError_whenSmallClaimHearingValidatesDatesAndSmallClaimHearingIsNotNull() {
            // Given
            UnavailableDate unavailableDate = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.now().minusDays(5)).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            SmallClaimHearing hearing = SmallClaimHearing.builder()
                .unavailableDatesRequired(YES)
                .smallClaimUnavailableDate(unavailableDates)
                .build();
            // When  // Then
            assertThat(validator.validateSmallClaimsHearing(hearing))
                .containsExactly("Unavailable Date cannot be past date");
        }

        @Test
        void shouldReturnError_whenSmallClaimHearingValidatesDatesAndDateInUnavaiableDateIsNull() {
            // Given
            UnavailableDate unavailableDate = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            SmallClaimHearing hearing = SmallClaimHearing.builder()
                .unavailableDatesRequired(YES)
                .smallClaimUnavailableDate(unavailableDates)
                .build();
            // When  // Then
            assertThat(validator.validateSmallClaimsHearing(hearing))
                .containsExactly("Details of unavailable date required");
        }

        @Test
        void shouldReturnError_whenSmallClaimHearingValidatesDatesAndDateRangeInUnavaiableDateIsNull() {
            // Given
            UnavailableDate unavailableDate = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.DATE_RANGE).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            SmallClaimHearing hearing = SmallClaimHearing.builder()
                .unavailableDatesRequired(YES)
                .smallClaimUnavailableDate(unavailableDates)
                .build();
            // When  // Then
            assertThat(validator.validateSmallClaimsHearing(hearing))
                .containsExactly("Details of unavailable date required");
        }

        @Test
        void shouldReturnError_whenSmallClaimHearingValidatesDatesAndDateRangeInUnavaiableDateIsNotValid() {
            // Given
            UnavailableDate unavailableDate = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                .fromDate(LocalDate.now().plusDays(5))
                .toDate(LocalDate.now().plusDays(4)).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            SmallClaimHearing hearing = SmallClaimHearing.builder()
                .unavailableDatesRequired(YES)
                .smallClaimUnavailableDate(unavailableDates)
                .build();
            // When  // Then
            assertThat(validator.validateSmallClaimsHearing(hearing))
                .containsExactly("From Date should be less than To Date");
        }

        @Test
        void shouldReturnNoError_whenSmallClaimHearingValidatesDatesAndFromDateIsLessThanToDate() {
            // Given: UnAvailableDate Where FromDate is less than ToDate
            UnavailableDate unavailableDate = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                .fromDate(LocalDate.now().plusDays(4))
                .toDate(LocalDate.now().plusDays(5)).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            SmallClaimHearing hearing = SmallClaimHearing.builder()
                .unavailableDatesRequired(YES)
                .smallClaimUnavailableDate(unavailableDates)
                .build();
            // When: Validate is called
            // Then: It should not return any errors
            assertThat(validator.validateSmallClaimsHearing(hearing))
                .isEmpty();
        }

        @Test
        void shouldReturnError_whenSmallClaimHearingValidatesDatesAndDateRangeInUnavaiableToDateIsNull() {
            // Given: SmallClaimHearing object with ToDate is null
            UnavailableDate unavailableDate = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                .fromDate(LocalDate.now().plusDays(5)).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            SmallClaimHearing hearing = SmallClaimHearing.builder()
                .unavailableDatesRequired(YES)
                .smallClaimUnavailableDate(unavailableDates)
                .build();
            // When: Validate is called
            // Then: It should return validation error message
            assertThat(validator.validateSmallClaimsHearing(hearing))
                .containsExactly("Details of unavailable date required");
        }

        @Test
        void shouldReturnError_whenSmallClaimHearingFromDateIsNull() {
            // Given: SmallClaimHearing object with FromDate is null
            UnavailableDate unavailableDate = UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                .toDate(LocalDate.now().plusDays(4)).build();
            List<Element<UnavailableDate>> unavailableDates = wrapElements(unavailableDate);
            SmallClaimHearing hearing = SmallClaimHearing.builder()
                .unavailableDatesRequired(YES)
                .smallClaimUnavailableDate(unavailableDates)
                .build();
            // When: Validate is called
            // Then: It should return validation error message
            assertThat(validator.validateSmallClaimsHearing(hearing))
                .containsExactly("Details of unavailable date required");
        }

        @Test
        void shouldReturnEmptyErrorList_whenNoUnavailableDates() {
            // Given: SmallClaimHearing with No Unavailable Dates
            SmallClaimHearing hearing = SmallClaimHearing.builder()
                .unavailableDatesRequired(NO)
                .build();
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
