package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import uk.gov.hmcts.reform.civil.config.properties.AsyncHandlerProperties;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncHandlerConfigurationTest {

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
            var asyncHandlerBean = it.getBean("asyncHandlerExecutor");

            assertThat(asyncHandlerBean).extracting("queueCapacity")
                    .isEqualTo(QUEUE_SIZE);
            assertThat(asyncHandlerBean).extracting("corePoolSize")
                .isEqualTo(CORE_POOL_SIZE);
            assertThat(asyncHandlerBean).extracting("maxPoolSize")
                .isEqualTo(MAX_POOL_SIZE);
        });
    }
}
