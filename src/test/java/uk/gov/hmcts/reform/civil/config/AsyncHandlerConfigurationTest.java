package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.reform.civil.config.properties.AsyncHandlerProperties;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class AsyncHandlerConfigurationTest {

    private static final int QUEUE_SIZE = 1;
    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 3;

    private static class TestAsyncHandlerProperties extends AsyncHandlerProperties {
        public TestAsyncHandlerProperties() {
            queueCapacity = QUEUE_SIZE;
            corePoolSize = CORE_POOL_SIZE;
            maxPoolSize = MAX_POOL_SIZE;
        }
    }

    ApplicationContextRunner context = new ApplicationContextRunner()
        .withUserConfiguration(TestAsyncHandlerProperties.class)
        .withUserConfiguration(AsyncHandlerConfiguration.class);

    @Test
    void shouldCheckPresenceOfBeansAndConfiguration_WhenAsyncHandlerConfigurationIsLoaded() {
        context.run(it -> {
            assertThat(it).hasSingleBean(Executor.class);
            assertThat(it).hasBean("asyncHandlerRejectedCount");
            var asyncHandlerBean = it.getBean("asyncHandlerExecutor", ThreadPoolTaskExecutor.class);

            assertThat(asyncHandlerBean).extracting("queueCapacity")
                    .isEqualTo(QUEUE_SIZE);
            assertThat(asyncHandlerBean).extracting("corePoolSize")
                .isEqualTo(CORE_POOL_SIZE);
            assertThat(asyncHandlerBean).extracting("maxPoolSize")
                .isEqualTo(MAX_POOL_SIZE);
        });
    }

    @Test
    void shouldIncrementRejectedCountAndThrowException_WhenTaskIsRejected() {
        context.run(it -> {
            ThreadPoolTaskExecutor executor = it.getBean("asyncHandlerExecutor", ThreadPoolTaskExecutor.class);
            AtomicLong rejectedCount = it.getBean("asyncHandlerRejectedCount", AtomicLong.class);

            assertThat(rejectedCount.get()).isZero();

            Runnable runnable = mock(Runnable.class);
            var threadPoolExecutor = executor.getThreadPoolExecutor();
            var rejectedExecutionHandler = threadPoolExecutor.getRejectedExecutionHandler();

            assertThatThrownBy(() -> rejectedExecutionHandler.rejectedExecution(runnable, threadPoolExecutor))
                .isInstanceOf(RejectedExecutionException.class)
                .hasMessageContaining("rejected from");

            assertThat(rejectedCount.get()).isEqualTo(1L);
        });
    }
}
