package uk.gov.hmcts.reform.civil.testsupport.mockito;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.mock.mockito.MockBeans;

/**
 * Container annotation that delegates to the underlying {@link MockBeans} infrastructure.
 */
@SuppressWarnings("removal")
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@MockBeans({})
public @interface MockitoBeans {
    MockitoBean[] value();
}
