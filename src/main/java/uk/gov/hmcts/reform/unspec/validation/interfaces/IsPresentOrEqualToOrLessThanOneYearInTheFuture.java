package uk.gov.hmcts.reform.unspec.validation.interfaces;

import uk.gov.hmcts.reform.unspec.validation.UnavailableDateValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UnavailableDateValidator.class)
public @interface IsPresentOrEqualToOrLessThanOneYearInTheFuture {
    String message() default "The date cannot be in the past and must not be more than a year in the future";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
