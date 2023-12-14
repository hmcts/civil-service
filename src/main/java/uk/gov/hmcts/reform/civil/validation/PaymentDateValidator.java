package uk.gov.hmcts.reform.civil.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.validation.groups.PaymentDateGroup;

import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

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
