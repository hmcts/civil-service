package uk.gov.hmcts.reform.civil.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.validation.groups.DateOfBirthGroup;

import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class DateOfBirthValidator {

    private final Validator validator;

    public List<String> validate(Party party) {
        return validator.validate(party, DateOfBirthGroup.class).stream()
            .map(ConstraintViolation::getMessage)
            .collect(toList());
    }
}
