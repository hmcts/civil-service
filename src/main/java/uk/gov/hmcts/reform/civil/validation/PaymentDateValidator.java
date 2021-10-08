package uk.gov.hmcts.reform.civil.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.validation.groups.DateOfBirthGroup;
import uk.gov.hmcts.reform.civil.validation.groups.PaymentDateGroup;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class PaymentDateValidator {

    private final Validator validator;

    public List<String> validate(Object obj) {
        return validator.validate(obj, PaymentDateGroup.class).stream()
            .map(ConstraintViolation::getMessage)
            .collect(toList());
    }
}
