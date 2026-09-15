package uk.gov.hmcts.reform.civil.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.backoff.BackoffStrategy;
import org.camunda.bpm.client.backoff.ErrorAwareBackoffStrategy;
import org.camunda.bpm.client.exception.ExternalTaskClientException;
import org.camunda.bpm.client.task.ExternalTask;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalTaskListenerConfigurationTest {

    private static final List<ExternalTask> NO_TASKS = Collections.emptyList();
    private static final List<ExternalTask> SOME_TASKS = Arrays.asList((ExternalTask) null);
    private static final ExternalTaskClientException AN_ERROR =
        new ExternalTaskClientException("gateway returned 502");

    ApplicationContextRunner context = new ApplicationContextRunner()
        .withPropertyValues("feign.client.config.processInstance.url=http://localhost")
        .withUserConfiguration(TestAuthTokenGeneratorImpl.class)
        .withUserConfiguration(ExternalTaskListenerConfiguration.class)
        .withUserConfiguration(TestConfig.class);

    @Test
    void shouldCheckPresenceOfBeans_WhenExternalTaskConfigurationIsLoaded() {
        context.run(it -> {
            assertThat(it).hasSingleBean(ExternalTaskClient.class);
            assertThat(it).hasSingleBean(BackoffStrategy.class);
            assertThat(it.getBean(BackoffStrategy.class)).isInstanceOf(ErrorAwareBackoffStrategy.class);
        });
    }

    @Test
    void backoffStrategy_shouldStayZeroWhileFetchesSucceedOrReturnNothing() {
        context.run(it -> {
            ErrorAwareBackoffStrategy strategy = errorAwareStrategy(it.getBean(BackoffStrategy.class));

            strategy.reconfigure(SOME_TASKS, null);
            assertThat(strategy.calculateBackoffTime()).isZero();

            strategy.reconfigure(NO_TASKS, null);
            assertThat(strategy.calculateBackoffTime()).isZero();
        });
    }

    @Test
    void backoffStrategy_shouldRampExponentiallyOnConsecutiveErrorsAndCapAtMax() {
        context.run(it -> {
            ErrorAwareBackoffStrategy strategy = errorAwareStrategy(it.getBean(BackoffStrategy.class));

            strategy.reconfigure(NO_TASKS, AN_ERROR);
            assertThat(strategy.calculateBackoffTime()).isEqualTo(500L);

            strategy.reconfigure(NO_TASKS, AN_ERROR);
            assertThat(strategy.calculateBackoffTime()).isEqualTo(1000L);

            strategy.reconfigure(NO_TASKS, AN_ERROR);
            assertThat(strategy.calculateBackoffTime()).isEqualTo(2000L);

            strategy.reconfigure(NO_TASKS, AN_ERROR);
            assertThat(strategy.calculateBackoffTime()).isEqualTo(4000L);

            // 500 * 2^4 = 8000 -> capped at the configured max of 5000
            strategy.reconfigure(NO_TASKS, AN_ERROR);
            assertThat(strategy.calculateBackoffTime()).isEqualTo(5000L);
        });
    }

    @Test
    void backoffStrategy_shouldResetToZeroAfterANonErrorFetch() {
        context.run(it -> {
            ErrorAwareBackoffStrategy strategy = errorAwareStrategy(it.getBean(BackoffStrategy.class));

            strategy.reconfigure(NO_TASKS, AN_ERROR);
            strategy.reconfigure(NO_TASKS, AN_ERROR);
            assertThat(strategy.calculateBackoffTime()).isPositive();

            strategy.reconfigure(NO_TASKS, null);
            assertThat(strategy.calculateBackoffTime()).isZero();
        });
    }

    @Test
    void backoffStrategy_singleArgReconfigureIsTreatedAsANonError() {
        context.run(it -> {
            BackoffStrategy strategy = it.getBean(BackoffStrategy.class);

            strategy.reconfigure(NO_TASKS);

            assertThat(strategy.calculateBackoffTime()).isZero();
        });
    }

    private static ErrorAwareBackoffStrategy errorAwareStrategy(BackoffStrategy strategy) {
        return (ErrorAwareBackoffStrategy) strategy;
    }

    @Configuration
    static class TestConfig {
        @Bean
        public EventProperties eventProperties() {
            EventProperties props = new EventProperties();
            props.setResponseTimeout(29500);
            props.setLockDuration(1980000);
            props.setClientBackoffInitial(500);
            props.setClientBackoffFactor(2);
            props.setClientBackoffMax(5000);
            props.setHttpValidateAfterInactivityMs(2000);
            return props;
        }
    }

    private static class TestAuthTokenGeneratorImpl implements AuthTokenGenerator {

        @Override
        public String generate() {
            return null;
        }
    }
}
