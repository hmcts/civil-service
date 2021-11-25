package uk.gov.hmcts.reform.civil.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.SmallClaimUnavailableDate;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.validation.groups.UnavailableDateGroup;
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
            unavailableDates.forEach(element -> validator.validate(element.getValue(), UnavailableDateGroup.class)
                .forEach(violation -> errors.add(violation.getMessage())));
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
            List<Element<SmallClaimUnavailableDate>> smallUnavailableDates
                = smallClaimHearing.getSmallClaimUnavailableDate();
            smallUnavailableDates.forEach(element -> {
                SmallClaimUnavailableDate smallClaimUnavailableDateElement = element.getValue();
                if (checkOneYearValidation(smallClaimUnavailableDateElement.getDate())
                    || checkOneYearValidation(smallClaimUnavailableDateElement.getFromDate())
                    || checkOneYearValidation(smallClaimUnavailableDateElement.getToDate())
                ) {
                    errors.add("Dates must be within the next 12 months.");
                } else if (checkPastDateValidation(smallClaimUnavailableDateElement.getDate())
                    ||
                    checkPastDateValidation(smallClaimUnavailableDateElement.getToDate())
                    ||
                    checkPastDateValidation(smallClaimUnavailableDateElement.getFromDate())) {
                    errors.add("Unavailable Date cannot be past date");
                } else if (smallClaimUnavailableDateElement.getFromDate() != null
                    && smallClaimUnavailableDateElement.getToDate() != null
                    && smallClaimUnavailableDateElement.getFromDate()
                    .isAfter(smallClaimUnavailableDateElement.getToDate())) {
                    errors.add("From Date should be less than To Date");
                }
            });
        }

        return errors;
    }

    private boolean checkOneYearValidation(LocalDate localDate) {
        if (localDate != null && localDate.isAfter(LocalDate.now().plusYears(1))) {
            return true;
        }
        return false;
    }

    private boolean checkPastDateValidation(LocalDate localDate) {
        if (localDate != null && localDate.isBefore(LocalDate.now())) {
            return true;
        }
        return false;
    }

    private boolean isNullOrEmpty(Hearing hearing) {
        List<Element<UnavailableDate>> unavailableDates = ofNullable(hearing.getUnavailableDates()).orElse(emptyList());
        return unavailableDates.isEmpty();
    }

    private boolean isSmallClaimHearingNullOrEmpty(SmallClaimHearing smallClaimHearing) {
        List<Element<SmallClaimUnavailableDate>> smallClaimUnavailableDates
            = ofNullable(smallClaimHearing.getSmallClaimUnavailableDate()).orElse(
            emptyList());
        return smallClaimUnavailableDates.isEmpty();
    }
}
