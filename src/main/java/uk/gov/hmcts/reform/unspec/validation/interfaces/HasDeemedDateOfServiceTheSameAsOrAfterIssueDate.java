package uk.gov.hmcts.reform.unspec.validation.interfaces;

import uk.gov.hmcts.reform.unspec.validation.HasDeemedDateOfServiceTheSameAsOrAfterIssueDateValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HasDeemedDateOfServiceTheSameAsOrAfterIssueDateValidator.class)
public @interface HasDeemedDateOfServiceTheSameAsOrAfterIssueDate {
    String message() default "The date must not be before issue date of claim";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
