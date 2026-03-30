package uk.gov.hmcts.reform.civil.validation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.validation.interfaces.IsPresentOrEqualToOrLessThanOneYearInTheFuture;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
public class UnavailableDateValidator implements
    ConstraintValidator<IsPresentOrEqualToOrLessThanOneYearInTheFuture, UnavailableDate> {

    private static final String DETAILS_OF_UNAVAILABLE_DATE_REQUIRED = "Details of unavailable date required";
    private static final String DATES_MUST_BE_WITHIN_NEXT_12_MONTHS = "Dates must be within the next 12 months.";
    private static final String UNAVAILABLE_DATE_CANNOT_BE_PAST = "Unavailable Date cannot be past date";
    private static final String FROM_DATE_SHOULD_BE_LESS_THAN_TO_DATE = "From Date should be less than To Date";
    private static final String FIRST_PAYMENT_DATE_ERROR = "Date of first payment must be today or within the next 12 months";

    @Override
    public boolean isValid(UnavailableDate value, ConstraintValidatorContext context) {
        LocalDate date = value.getDate();

        return date.isAfter(LocalDate.now().minusDays(1)) && date.isBefore(LocalDate.now().plusYears(1).plusDays(1));
    }

    public List<String> validate(Hearing hearing) {
        List<String> errors = new ArrayList<>();
        if (hearing.getUnavailableDatesRequired() == YES && isNullOrEmpty(hearing)) {
            errors.add(DETAILS_OF_UNAVAILABLE_DATE_REQUIRED);
        }

        if (hearing.getUnavailableDatesRequired() == YES && !isNullOrEmpty(hearing)) {
            List<Element<UnavailableDate>> unavailableDates = hearing.getUnavailableDates();
            errors = dateValidation(unavailableDates);
        }

        return errors;
    }

    public List<String> validateFastClaimHearing(Hearing hearingLRspec) {
        List<String> errors = new ArrayList<>();
        if ((hearingLRspec != null && hearingLRspec.getUnavailableDatesRequired() == YES)) {
            if (!isFastClaimHearingNullOrEmpty(hearingLRspec)) {
                List<Element<UnavailableDate>> unavailableDate = hearingLRspec.getUnavailableDates();
                errors = dateValidation(unavailableDate);
            } else {
                errors.add(DETAILS_OF_UNAVAILABLE_DATE_REQUIRED);
            }
        }
        return errors;
    }

    public List<String> validateSmallClaimsHearing(SmallClaimHearing smallClaimHearing) {
        List<String> errors = new ArrayList<>();
        if (smallClaimHearing.getUnavailableDatesRequired() == YES
            && isSmallClaimHearingNullOrEmpty(smallClaimHearing)) {
            errors.add(DETAILS_OF_UNAVAILABLE_DATE_REQUIRED);
        }
        if (smallClaimHearing.getUnavailableDatesRequired() == YES
            && !isSmallClaimHearingNullOrEmpty(smallClaimHearing)) {
            List<Element<UnavailableDate>> smallUnavailableDates
                = smallClaimHearing.getSmallClaimUnavailableDate();

            errors = dateValidation(smallUnavailableDates);
        }
        return errors;
    }

    public List<String> validateAdditionalUnavailableDates(List<Element<UnavailableDate>> dates) {
        List<Element<UnavailableDate>> unavailableDates = ofNullable(dates).orElse(emptyList());
        if (unavailableDates.isEmpty()) {
            return List.of(DETAILS_OF_UNAVAILABLE_DATE_REQUIRED);
        }

        return dateValidation(unavailableDates);
    }

    private boolean checkOneYearValidation(LocalDate localDate) {
        return localDate != null && localDate.isAfter(LocalDate.now().plusYears(1));
    }

    private boolean checkPastDateValidation(LocalDate localDate) {
        return localDate != null && localDate.isBefore(LocalDate.now());
    }

    private boolean isNullOrEmpty(Hearing hearing) {
        List<Element<UnavailableDate>> unavailableDates = ofNullable(hearing.getUnavailableDates()).orElse(emptyList());
        return unavailableDates.isEmpty();
    }

    private boolean isFastClaimHearingNullOrEmpty(Hearing hearingLRspec) {
        List<Element<UnavailableDate>> unavailableDates
            = ofNullable(hearingLRspec.getUnavailableDates()).orElse(emptyList());
        return unavailableDates.isEmpty();
    }

    private boolean isSmallClaimHearingNullOrEmpty(SmallClaimHearing smallClaimHearing) {
        List<Element<UnavailableDate>> smallClaimUnavailableDates
            = ofNullable(smallClaimHearing.getSmallClaimUnavailableDate()).orElse(
            emptyList());
        return smallClaimUnavailableDates.isEmpty();
    }

    private List<String> dateValidation(List<Element<UnavailableDate>> unavailableDate) {
        List<String> errors = new ArrayList<>();
        unavailableDate.forEach(element -> validateUnavailableDate(errors, element.getValue()));
        return errors;
    }

    private void validateUnavailableDate(List<String> errors, UnavailableDate unavailableDate) {
        addMissingDateError(errors, unavailableDate);
        addDateRangeErrors(errors, unavailableDate);
    }

    private void addMissingDateError(List<String> errors, UnavailableDate unavailableDate) {
        if (hasMissingDate(unavailableDate)) {
            errors.add(DETAILS_OF_UNAVAILABLE_DATE_REQUIRED);
        }
    }

    private void addDateRangeErrors(List<String> errors, UnavailableDate unavailableDate) {
        if (hasDateBeyondOneYear(unavailableDate)) {
            errors.add(DATES_MUST_BE_WITHIN_NEXT_12_MONTHS);
        } else if (hasPastDate(unavailableDate)) {
            errors.add(UNAVAILABLE_DATE_CANNOT_BE_PAST);
        } else if (hasInvalidDateRangeOrder(unavailableDate)) {
            errors.add(FROM_DATE_SHOULD_BE_LESS_THAN_TO_DATE);
        }
    }

    private boolean hasMissingDate(UnavailableDate unavailableDate) {
        return hasMissingSingleDate(unavailableDate) || hasMissingDateRange(unavailableDate);
    }

    private boolean hasMissingSingleDate(UnavailableDate unavailableDate) {
        return UnavailableDateType.SINGLE_DATE == unavailableDate.getUnavailableDateType()
            && unavailableDate.getDate() == null;
    }

    private boolean hasMissingDateRange(UnavailableDate unavailableDate) {
        return UnavailableDateType.DATE_RANGE == unavailableDate.getUnavailableDateType()
            && (unavailableDate.getFromDate() == null || unavailableDate.getToDate() == null);
    }

    private boolean hasDateBeyondOneYear(UnavailableDate unavailableDate) {
        return checkOneYearValidation(unavailableDate.getDate())
            || checkOneYearValidation(unavailableDate.getFromDate())
            || checkOneYearValidation(unavailableDate.getToDate());
    }

    private boolean hasPastDate(UnavailableDate unavailableDate) {
        return checkPastDateValidation(unavailableDate.getDate())
            || checkPastDateValidation(unavailableDate.getToDate())
            || checkPastDateValidation(unavailableDate.getFromDate());
    }

    private boolean hasInvalidDateRangeOrder(UnavailableDate unavailableDate) {
        return unavailableDate.getFromDate() != null
            && unavailableDate.getToDate() != null
            && unavailableDate.getFromDate().isAfter(unavailableDate.getToDate());
    }

    public List<String> validateFuturePaymentDate(LocalDate paymentDate) {
        List<String> errors = new ArrayList<>();

        if (checkOneYearValidation(paymentDate) || checkPastDateValidation(paymentDate)) {
            errors.add(FIRST_PAYMENT_DATE_ERROR);
        }
        return errors;
    }
}
