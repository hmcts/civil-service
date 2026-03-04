package uk.gov.hmcts.reform.civil.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Value;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {

    /**
     * The maximum number of requests allowed within the time window.
     * Default is 100 requests.
     */
    @Value("${rateLimiter.rateLimit}")
    int rateLimit() default 5;

    /**
     * The time in seconds during which the limit applies.
     * Default is 60 seconds (1 minute).
     */
    @Value("${rateLimiter.timeInSeconds}")
    int timeInSeconds() default 60;
}
