package uk.gov.hmcts.reform.civil.testutils;

import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.core.Ordered;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Spring Boot 4 removed MockitoTestExecutionListener, which initialised {@code @Mock}, {@code @Spy},
 * {@code @Captor} and {@code @InjectMocks} fields for tests run with SpringExtension only. This restores
 * that behaviour so those tests do not need migrating to MockitoExtension in the same change.
 */
public class MockitoAnnotationsTestExecutionListener extends AbstractTestExecutionListener {

    private static final String MOCKS_ATTRIBUTE = MockitoAnnotationsTestExecutionListener.class.getName() + ".mocks";
    private static final Set<Class<? extends Annotation>> MOCKITO_ANNOTATIONS =
        Set.of(Mock.class, Spy.class, Captor.class, InjectMocks.class);

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    @Override
    public void prepareTestInstance(TestContext testContext) {
        Object testInstance = testContext.getTestInstance();
        if (hasMockitoAnnotations(testInstance.getClass())) {
            testContext.setAttribute(MOCKS_ATTRIBUTE, MockitoAnnotations.openMocks(testInstance));
        }
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        Object mocks = testContext.removeAttribute(MOCKS_ATTRIBUTE);
        if (mocks instanceof AutoCloseable closeable) {
            closeable.close();
        }
    }

    private static boolean hasMockitoAnnotations(Class<?> testClass) {
        AtomicBoolean found = new AtomicBoolean();
        ReflectionUtils.doWithFields(testClass, field -> {
            for (Class<? extends Annotation> annotation : MOCKITO_ANNOTATIONS) {
                if (field.isAnnotationPresent(annotation)) {
                    found.set(true);
                }
            }
        });
        return found.get();
    }
}
