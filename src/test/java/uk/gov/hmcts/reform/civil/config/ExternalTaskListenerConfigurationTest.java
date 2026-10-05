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
            assertThat(it).hasSingleBean(BackoffStrategy.class);
            assertThat(it.getBean(BackoffStrategy.class)).isInstanceOf(ErrorAwareBackoffStrategy.class);
        });
    }

    /**
     * Three clients now: one real client per thread, plus the router the listeners inject. The
     * router must be the one resolved by type, because all 39 listeners take a bare
     * {@code ExternalTaskClient} and would otherwise bind to a single underlying client and lose
     * the split.
     */
    @Test
    void shouldExposeTwoRealClientsAndRouteThroughThePrimaryOne() {
        context.run(it -> {
            assertThat(it.getBeanNamesForType(ExternalTaskClient.class))
                .containsExactlyInAnyOrder("client", "caseDrivenExternalTaskClient", "schedulerExternalTaskClient");

            assertThat(it.getBean(ExternalTaskClient.class))
                .as("the bean injected by type must be the router, not one of the underlying clients")
                .isInstanceOf(TopicRoutingExternalTaskClient.class);
        });
    }

    @Test
    void shouldRouteSchedulerTopicsAwayFromCaseDrivenTopics() {
        context.run(it -> {
            TopicRoutingExternalTaskClient router =
                (TopicRoutingExternalTaskClient) it.getBean(ExternalTaskClient.class);
            ExternalTaskClient caseDriven = (ExternalTaskClient) it.getBean("caseDrivenExternalTaskClient");
            ExternalTaskClient scheduler = (ExternalTaskClient) it.getBean("schedulerExternalTaskClient");

            assertThat(router.clientFor("BUNDLE_CREATION_CHECK")).isSameAs(scheduler);
            assertThat(router.clientFor("AUTOMATED_HEARING_NOTICE")).isSameAs(scheduler);
            assertThat(router.clientFor("START_BUSINESS_PROCESS")).isSameAs(caseDriven);
            assertThat(router.clientFor("processCaseEvent")).isSameAs(caseDriven);

            assertThat(caseDriven)
                .as("the two underlying clients must be distinct, or there is still one thread")
                .isNotSameAs(scheduler);
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

    /**
     * Each client must hold its own backoff counter. CamundaErrorAwareBackoffStrategy keeps the
     * consecutive error count in an AtomicInteger, so a shared instance would let the scheduler
     * client's errors back off the case driven client, which is precisely the cross contamination
     * the split exists to prevent.
     *
     * <p>A built Camunda client does not expose its strategy, so this asserts the factory the two
     * client beans call: it must hand back a new instance each time, with an independent counter.
     */
    @Test
    void shouldHandOutAnIndependentBackoffStrategyPerClient() {
        ExternalTaskListenerConfiguration config =
            new ExternalTaskListenerConfiguration("http://localhost", () -> null, eventProperties());

        ErrorAwareBackoffStrategy first = errorAwareStrategy(config.newBackoffStrategy());
        ErrorAwareBackoffStrategy second = errorAwareStrategy(config.newBackoffStrategy());

        assertThat(first).isNotSameAs(second);

        first.reconfigure(NO_TASKS, AN_ERROR);
        first.reconfigure(NO_TASKS, AN_ERROR);

        assertThat(first.calculateBackoffTime())
            .as("the strategy that saw the errors backs off")
            .isPositive();
        assertThat(second.calculateBackoffTime())
            .as("the other client's strategy must be unaffected by them")
            .isZero();
    }

    private static EventProperties eventProperties() {
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
