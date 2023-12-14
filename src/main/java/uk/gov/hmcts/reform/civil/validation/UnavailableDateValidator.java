package uk.gov.hmcts.reform.civil.validation;

import lombok.RequiredArgsConstructor;
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
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Validator;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
@RequiredArgsConstructor
public class UnavailableDateValidator implements
    ConstraintValidator<IsPresentOrEqualToOrLessThanOneYearInTheFuture, UnavailableDate> {

    private final Validator validator;

    @Override
    public boolean isValid(UnavailableDate value, ConstraintValidatorContext context) {
        LocalDate date = value.getDate();

        return date.isAfter(LocalDate.now().minusDays(1)) && date.isBefore(LocalDate.now().plusYears(1).plusDays(1));
    }

    public List<String> validate(Hearing hearing) {
        List<String> errors = new ArrayList<>();
        if (hearing.getUnavailableDatesRequired() == YES && isNullOrEmpty(hearing)) {
            errors.add("Details of unavailable date required");
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
                errors.add("Details of unavailable date required");
            }
        }
        return errors;
    }

    public List<String> validateSmallClaimsHearing(SmallClaimHearing smallClaimHearing) {
        List<String> errors = new ArrayList<>();
        if (smallClaimHearing.getUnavailableDatesRequired() == YES
            && isSmallClaimHearingNullOrEmpty(smallClaimHearing)) {
            errors.add("Details of unavailable date required");
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
            return List.of("Details of unavailable date required");
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
        unavailableDate.forEach(element -> {
            UnavailableDate unavailableDateElement = element.getValue();
            if (UnavailableDateType.SINGLE_DATE == unavailableDateElement.getUnavailableDateType()
                && unavailableDateElement.getDate() == null) {
                errors.add("Details of unavailable date required");
            }
            if (UnavailableDateType.DATE_RANGE == unavailableDateElement.getUnavailableDateType()) {
                if (unavailableDateElement.getFromDate() == null
                    || unavailableDateElement.getToDate() == null) {
                    errors.add("Details of unavailable date required");
                }
            }
            if (checkOneYearValidation(unavailableDateElement.getDate())
                || checkOneYearValidation(unavailableDateElement.getFromDate())
                || checkOneYearValidation(unavailableDateElement.getToDate())
            ) {
                errors.add("Dates must be within the next 12 months.");
            } else if (checkPastDateValidation(unavailableDateElement.getDate())
                ||
                checkPastDateValidation(unavailableDateElement.getToDate())
                ||
                checkPastDateValidation(unavailableDateElement.getFromDate())) {
                errors.add("Unavailable Date cannot be past date");
            } else if (unavailableDateElement.getFromDate() != null
                && unavailableDateElement.getToDate() != null
                && unavailableDateElement.getFromDate()
                .isAfter(unavailableDateElement.getToDate())) {
                errors.add("From Date should be less than To Date");
            }
        });
        return errors;
    }

    public List<String> validateFuturePaymentDate(LocalDate paymentDate) {
        List<String> errors = new ArrayList<>();

        if (checkOneYearValidation(paymentDate) || checkPastDateValidation(paymentDate)) {
            errors.add("Date of first payment must be today or within the next 12 months");
        }
        return errors;
    }

}
