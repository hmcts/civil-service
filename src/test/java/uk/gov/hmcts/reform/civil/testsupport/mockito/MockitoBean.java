package uk.gov.hmcts.reform.civil.testsupport.mockito;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mockito.Answers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.annotation.AliasFor;

/**
 * Temporary bridge annotation that mirrors the Spring Boot {@link MockBean} contract while
 * suppressing the for-removal compiler warnings introduced in Spring Boot 3.5. Once the official
 * replacement lands we can migrate to it and delete this shim from the tests.
 */
@SuppressWarnings("removal")
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@java.lang.annotation.Repeatable(MockitoBeans.class)
@MockBean
public @interface MockitoBean {

    @AliasFor(annotation = MockBean.class, attribute = "name")
    String name() default "";

    @AliasFor(annotation = MockBean.class, attribute = "value")
    Class<?>[] value() default {};

    @AliasFor(annotation = MockBean.class, attribute = "classes")
    Class<?>[] classes() default {};

    @AliasFor(annotation = MockBean.class, attribute = "extraInterfaces")
    Class<?>[] extraInterfaces() default {};

    @AliasFor(annotation = MockBean.class, attribute = "answer")
    Answers answer() default Answers.RETURNS_DEFAULTS;

    @AliasFor(annotation = MockBean.class, attribute = "serializable")
    boolean serializable() default false;

    @AliasFor(annotation = MockBean.class, attribute = "reset")
    org.springframework.boot.test.mock.mockito.MockReset reset()
        default org.springframework.boot.test.mock.mockito.MockReset.AFTER;
}
