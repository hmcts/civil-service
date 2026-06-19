package uk.gov.hmcts.reform.civil.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.properties.EventProperties;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalTaskListenerConfigurationTest {

    ApplicationContextRunner context = new ApplicationContextRunner()
        .withPropertyValues("feign.client.config.processInstance.url=http://localhost")
        .withUserConfiguration(TestAuthTokenGeneratorImpl.class)
        .withUserConfiguration(ExternalTaskListenerConfiguration.class)
        .withUserConfiguration(TestConfig.class);

    @Test
    void shouldCheckPresenceOfBeans_WhenExternalTaskConfigurationIsLoaded() {
        context.run(it -> assertThat(it).hasSingleBean(ExternalTaskClient.class));
    }

    @Configuration
    static class TestConfig {
        @Bean
        public EventProperties eventProperties() {
            EventProperties props = new EventProperties();
            props.setResponseTimeout(29500);
            props.setLockDuration(1980000);
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
