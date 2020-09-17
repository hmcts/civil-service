package uk.gov.hmcts.reform.unspec.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalTaskListenerConfigurationTest {

    ApplicationContextRunner context = new ApplicationContextRunner()
        .withUserConfiguration(ExternalTaskListenerConfiguration.class);

    @Test
    void shouldCheckPresenceOfBeans_WhenExternalTaskConfigurationIsLoaded() {
        context.run(it -> assertThat(it).hasSingleBean(ExternalTaskClient.class));
    }
}
