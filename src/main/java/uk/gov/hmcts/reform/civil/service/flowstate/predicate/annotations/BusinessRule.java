package uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SuppressWarnings("UnusedReturnValue")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface BusinessRule {
    String summary();                   // Short readable name
    String description();               // detailed explanation for BAs
    String group() default "General";   // To group in the report (e.g., "Payment")
}
