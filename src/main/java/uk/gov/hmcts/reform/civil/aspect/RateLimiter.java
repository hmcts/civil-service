package uk.gov.hmcts.reform.civil.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {

    /**
     * The maximum number of requests allowed within the time window.
     * Default is -1 and will be used from config value.
     */
    int rateLimit() default -1;

    /**
     * The time in seconds during which the limit applies.
     * Default is -1 and will be used from config value.
     */
    int timeInSeconds() default -1;
}
