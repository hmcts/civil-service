package uk.gov.hmcts.reform.civil.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
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

    /**
     * The defaults are declared twice: as a field initialiser, and again in application.yaml as the
     * fallback of {@code ${EVENT_CASE_DRIVEN_CLIENTS:n}}. Only the yaml one runs in production,
     * because binding overwrites the field whenever the key is present, and only the field one is
     * seen by tests that construct EventProperties directly. So they can disagree with nothing
     * failing: changing the yaml to 3 leaves every other test in this repo green while production
     * runs 3.
     *
     * <p>This loads the real application.yaml and asserts the value that production actually gets,
     * which is the only assertion here that would catch that drift.
     */
    @Test
    void applicationYamlShouldDeclareTheSameDefaultsAsTheFields() {
        new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withUserConfiguration(TestConfig.class)
            .run(it -> {
                EventProperties bound = it.getBean(EventProperties.class);
                EventProperties fieldDefaults = new EventProperties();

                assertThat(bound.getCaseDrivenClients())
                    .as("async.event.caseDrivenClients in application.yaml must match the field default")
                    .isEqualTo(fieldDefaults.getCaseDrivenClients())
                    .isEqualTo(2);

                assertThat(bound.getSchedulerMaxTasks())
                    .as("the scheduler client is deliberately kept at one task per fetch")
                    .isEqualTo(1);
            });
    }

    @EnableConfigurationProperties(EventProperties.class)
    static class TestConfig {
    }
}
