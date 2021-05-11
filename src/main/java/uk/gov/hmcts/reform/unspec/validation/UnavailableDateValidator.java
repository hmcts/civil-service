package uk.gov.hmcts.reform.unspec.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.unspec.model.UnavailableDate;
import uk.gov.hmcts.reform.unspec.model.common.Element;
import uk.gov.hmcts.reform.unspec.model.dq.Hearing;
import uk.gov.hmcts.reform.unspec.validation.groups.UnavailableDateGroup;
import uk.gov.hmcts.reform.unspec.validation.interfaces.IsPresentOrEqualToOrLessThanOneYearInTheFuture;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Validator;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.YES;

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

    private boolean isNullOrEmpty(Hearing hearing) {
        List<Element<UnavailableDate>> unavailableDates = ofNullable(hearing.getUnavailableDates()).orElse(emptyList());
        return unavailableDates.isEmpty();
    }
}
