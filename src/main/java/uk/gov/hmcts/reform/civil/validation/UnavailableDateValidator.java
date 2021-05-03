package uk.gov.hmcts.reform.civil.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.validation.groups.UnavailableDateGroup;
import uk.gov.hmcts.reform.civil.validation.interfaces.IsPresentOrEqualToOrLessThanOneYearInTheFuture;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Validator;

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

    public List<String> validate(List<Element<UnavailableDate>> unavailableDates) {
        List<String> errors = new ArrayList<>();

        unavailableDates.forEach(element -> validator.validate(element.getValue(), UnavailableDateGroup.class)
            .forEach(violation -> errors.add(violation.getMessage())));

        return errors;
    }
}
