package uk.gov.hmcts.reform.civil.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the {@code async.event} property names against the field names they bind to.
 *
 * <p>Worth its own test because the failure is silent in the direction that matters: a misspelled
 * key in {@code application.yaml} leaves the field at its code default, and for
 * {@code caseDrivenClients} the code default is a value we would plausibly want anyway, so the
 * environment override would simply never take effect and nothing would say so. The bound value
 * asserted below is deliberately not the default, or the test would pass without binding anything.
 */
class EventPropertiesBindingTest {

    private final ApplicationContextRunner context = new ApplicationContextRunner()
        .withUserConfiguration(TestConfig.class);

    @Test
    void shouldBindTheCaseDrivenClientCount() {
        context.withPropertyValues("async.event.caseDrivenClients=5").run(it ->
            assertThat(it.getBean(EventProperties.class).getCaseDrivenClients()).isEqualTo(5));
    }

    @Test
    void shouldDefaultToTwoCaseDrivenClientsWhenNotConfigured() {
        context.run(it ->
            assertThat(it.getBean(EventProperties.class).getCaseDrivenClients()).isEqualTo(2));
    }

    @Test
    void shouldBindTheMaxTasksPerClient() {
        context.withPropertyValues(
            "async.event.caseDrivenMaxTasks=10",
            "async.event.schedulerMaxTasks=1"
        ).run(it -> {
            EventProperties props = it.getBean(EventProperties.class);
            assertThat(props.getCaseDrivenMaxTasks()).isEqualTo(10);
            assertThat(props.getSchedulerMaxTasks()).isEqualTo(1);
        });
    }

    @EnableConfigurationProperties(EventProperties.class)
    static class TestConfig {
    }
}
