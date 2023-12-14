package uk.gov.hmcts.reform.civil.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalTaskListenerConfigurationTest {

    ApplicationContextRunner context = new ApplicationContextRunner()
        .withUserConfiguration(TestAuthTokenGeneratorImpl.class)
        .withUserConfiguration(ExternalTaskListenerConfiguration.class);

    @Test
    void shouldCheckPresenceOfBeans_WhenExternalTaskConfigurationIsLoaded() {
        context.run(it -> assertThat(it).hasSingleBean(ExternalTaskClient.class));
    }

    private static class TestAuthTokenGeneratorImpl implements AuthTokenGenerator {

        @Override
        public String generate() {
            return null;
        }
    }
}
