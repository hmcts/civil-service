package uk.gov.hmcts.reform.civil.model.dq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HomeTypeValidator.class)
public @interface ValidHomeType {

    String message() default "Home type is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
