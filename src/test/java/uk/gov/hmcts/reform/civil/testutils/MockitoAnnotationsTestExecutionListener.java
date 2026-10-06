package uk.gov.hmcts.reform.civil.testutils;

import org.mockito.MockitoAnnotations;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Initialises Mockito annotations ({@code @Mock}, {@code @Spy}, {@code @Captor}, {@code @InjectMocks}) on
 * Spring-managed test instances.
 *
 * <p>Spring Boot 3 did this through its {@code MockitoTestExecutionListener}, which Spring Boot 4 removed.
 * Many tests here run with {@code SpringExtension} and rely on it, so this listener keeps that behaviour.
 * The annotations are looked up on the instance's own class rather than {@link TestContext#getTestClass()},
 * because Spring Framework 7 prepares enclosing {@code @Nested} instances with the nested class as the test
 * class. Registered in {@code META-INF/spring.factories}.
 */
public class MockitoAnnotationsTestExecutionListener extends AbstractTestExecutionListener {

    private static final String MOCKS_ATTRIBUTE_NAME = MockitoAnnotationsTestExecutionListener.class.getName()
        + ".mocks";

    @Override
    public int getOrder() {
        // Same position Spring Boot 3 used: before dependency injection (2000).
        return 1950;
    }

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        initMocks(testContext);
    }

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        if (Boolean.TRUE.equals(
            testContext.getAttribute(DependencyInjectionTestExecutionListener.REINJECT_DEPENDENCIES_ATTRIBUTE))) {
            initMocks(testContext);
        }
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        closeMocks(testContext);
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        closeMocks(testContext);
    }

    @SuppressWarnings("unchecked")
    private void initMocks(TestContext testContext) throws Exception {
        Object instance = testContext.getTestInstance();
        if (!hasMockitoAnnotations(instance.getClass())) {
            return;
        }
        Map<Object, AutoCloseable> mocks = (Map<Object, AutoCloseable>) testContext.computeAttribute(
            MOCKS_ATTRIBUTE_NAME, name -> new IdentityHashMap<Object, AutoCloseable>());
        AutoCloseable previous = mocks.remove(instance);
        if (previous != null) {
            previous.close();
        }
        mocks.put(instance, MockitoAnnotations.openMocks(instance));
    }

    @SuppressWarnings("unchecked")
    private void closeMocks(TestContext testContext) throws Exception {
        Object mocks = testContext.removeAttribute(MOCKS_ATTRIBUTE_NAME);
        if (mocks != null) {
            for (AutoCloseable closeable : ((Map<Object, AutoCloseable>) mocks).values()) {
                closeable.close();
            }
        }
    }

    private boolean hasMockitoAnnotations(Class<?> testClass) {
        AtomicBoolean found = new AtomicBoolean();
        ReflectionUtils.doWithFields(testClass, field -> {
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (annotation.annotationType().getName().startsWith("org.mockito")) {
                    found.set(true);
                }
            }
        });
        return found.get();
    }
}
