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
     * Only the router and the scheduler client may be resolvable as an {@code ExternalTaskClient}.
     * All 39 listeners take a bare {@code ExternalTaskClient}, so the case driven clients are
     * deliberately held inside {@link CaseDrivenExternalTaskClients}: were they beans of that type,
     * a listener could bind to one of them and be served by only a share of the threads.
     */
    @Test
    void shouldExposeOnlyTheRouterAndSchedulerClientByTypeAndRouteThroughThePrimaryOne() {
        context.run(it -> {
            assertThat(it.getBeanNamesForType(ExternalTaskClient.class))
                .containsExactlyInAnyOrder("client", "schedulerExternalTaskClient");

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
            List<ExternalTaskClient> caseDriven = it.getBean(CaseDrivenExternalTaskClients.class).clients();
            ExternalTaskClient scheduler = (ExternalTaskClient) it.getBean("schedulerExternalTaskClient");

            assertThat(router.clientsFor("BUNDLE_CREATION_CHECK")).containsExactly(scheduler);
            assertThat(router.clientsFor("AUTOMATED_HEARING_NOTICE")).containsExactly(scheduler);
            assertThat(router.clientsFor("START_BUSINESS_PROCESS")).isEqualTo(caseDriven);
            assertThat(router.clientsFor("processCaseEvent")).isEqualTo(caseDriven);

            assertThat(caseDriven)
                .as("case driven clients must be distinct from the scheduler one, or there is one thread")
                .doesNotContain(scheduler);
        });
    }

    /**
     * The default must actually be more than one, since that is the whole point of the change: the
     * single case driven thread was measured at 95% occupancy through a functional test run.
     */
    @Test
    void shouldBuildTwoDistinctCaseDrivenClientsByDefault() {
        context.run(it -> {
            List<ExternalTaskClient> clients = it.getBean(CaseDrivenExternalTaskClients.class).clients();

            assertThat(clients).hasSize(2);
            assertThat(clients.get(0))
                .as("two references to one client would still be one subscription thread")
                .isNotSameAs(clients.get(1));
        });
    }

    @Test
    void shouldHonourAConfiguredCaseDrivenClientCount() {
        contextWithCaseDrivenClients(4).run(it -> {
            assertThat(it.getBean(CaseDrivenExternalTaskClients.class).clients()).hasSize(4);
        });
    }

    /**
     * One client restores the previous single threaded behaviour, and must do so without taking a
     * different code path: the subscription is the client's own builder, not a fan out wrapper.
     */
    @Test
    void shouldRestoreSingleThreadedBehaviourWhenConfiguredWithOneClient() {
        contextWithCaseDrivenClients(1).run(it -> {
            TopicRoutingExternalTaskClient router =
                (TopicRoutingExternalTaskClient) it.getBean(ExternalTaskClient.class);

            assertThat(it.getBean(CaseDrivenExternalTaskClients.class).clients()).hasSize(1);
            assertThat(router.clientsFor("processCaseEvent")).hasSize(1);
        });
    }

    /**
     * Zero clients would leave every case driven topic unsubscribed, which is a silent loss of all
     * case processing, so the context must fail to start rather than come up degraded.
     */
    @Test
    void shouldFailToStartWhenConfiguredWithNoCaseDrivenClients() {
        contextWithCaseDrivenClients(0).run(it -> {
            assertThat(it).hasFailed();
            assertThat(it.getStartupFailure())
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .rootCause()
                .hasMessageContaining("caseDrivenClients must be at least 1");
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

    /**
     * The shared {@code TestConfig} supplies {@link EventProperties} as a hand built bean, so
     * {@code withPropertyValues} cannot reach it. This overrides that bean instead, which is what
     * actually varies the client count.
     */
    private ApplicationContextRunner contextWithCaseDrivenClients(int count) {
        EventProperties props = eventProperties();
        props.setCaseDrivenClients(count);
        return new ApplicationContextRunner()
            .withPropertyValues("feign.client.config.processInstance.url=http://localhost")
            .withUserConfiguration(TestAuthTokenGeneratorImpl.class)
            .withUserConfiguration(ExternalTaskListenerConfiguration.class)
            .withBean(EventProperties.class, () -> props);
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
            props.setCaseDrivenMaxTasks(10);
            props.setSchedulerMaxTasks(1);
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
        props.setCaseDrivenMaxTasks(10);
        props.setSchedulerMaxTasks(1);
        return props;
    }
}
