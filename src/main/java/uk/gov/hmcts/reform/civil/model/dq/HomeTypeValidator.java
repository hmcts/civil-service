package uk.gov.hmcts.reform.civil.model.dq;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.civil.enums.HomeTypeOptionSpec;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HomeTypeValidator implements ConstraintValidator<ValidHomeType, HomeDetails> {

    /**
     * Checks if HomeDetails type fields are valid.
     *
     * @param value   mandatory
     * @param context unused
     * @return true if and only if value.type is not null, and it is either different from OTHER
     *     or value.typeOtherDetails is not blank
     */
    @Override
    public boolean isValid(HomeDetails value, ConstraintValidatorContext context) {
        return value.getType() != null
            && (value.getType() != HomeTypeOptionSpec.OTHER
            || StringUtils.isNotBlank(value.getTypeOtherDetails()));
    }
}
