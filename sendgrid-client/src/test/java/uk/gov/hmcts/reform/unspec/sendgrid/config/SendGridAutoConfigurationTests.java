package uk.gov.hmcts.reform.unspec.sendgrid.config;

import com.sendgrid.SendGrid;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SendGridAutoConfigurationTests {

    private static final String API_KEY = "SEND.GRID.SECRET-API-KEY";
    private static final String MY_CUSTOM_API_KEY = "SEND.GRID.MY.CUSTOM_API_KEY";

    private AnnotationConfigApplicationContext context;

    @AfterEach
    void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    void shouldAutoCreateSendGridBean_whenApiKeyIsConfigured() {
        loadContext("sendgrid.api-key:" + API_KEY);
        SendGrid sendGrid = this.context.getBean(SendGrid.class);
        assertThat(sendGrid.getRequestHeaders()).containsEntry("Authorization", "Bearer " + API_KEY);
    }

    @Test
    void shouldNotFiredAutoConfigure_whenApiKeyIsNotConfigured() {
        loadContext();
        assertThatExceptionOfType(NoSuchBeanDefinitionException.class)
            .isThrownBy(() -> this.context.getBean(SendGrid.class));
    }

    @Test
    void shouldNotFiredAutoConfigure_whenBeanAlreadyCreatedManually() {
        loadContext(ManualSendGridConfiguration.class, "sendgrid.api-key:" + API_KEY);
        SendGrid sendGrid = this.context.getBean(SendGrid.class);
        assertThat(sendGrid.getRequestHeaders()).containsEntry("Authorization", "Bearer " + MY_CUSTOM_API_KEY);
    }

    @Test
    void shouldAutoCreateSendGridBeanWithTestEnabled_whenApiKeyAndTestPropertyIsConfigured() {
        loadContext("sendgrid.api-key:" + API_KEY, "sendgrid.test:true", "sendgrid.host:localhost");
        SendGrid sendGrid = this.context.getBean(SendGrid.class);
        assertThat(sendGrid).extracting("client").extracting("test").isEqualTo(true);
    }

    @Test
    void shouldAutoCreateSendGridBeanWithCustomHost_whenApiKeyAndHostPropertyIsConfigured() {
        loadContext("sendgrid.api-key:" + API_KEY, "sendgrid.host:localhost");
        SendGrid sendGrid = this.context.getBean(SendGrid.class);
        assertThat(sendGrid).extracting("host").isEqualTo("localhost");
    }

    @Test
    void shouldAutoCreateSendGridBeanWithCustomVersion_whenApiKeyAndVersionPropertyIsConfigured() {
        loadContext("sendgrid.api-key:" + API_KEY, "sendgrid.version:v2");
        SendGrid sendGrid = this.context.getBean(SendGrid.class);
        assertThat(sendGrid).extracting("version").isEqualTo("v2");
    }

    private void loadContext(String... environment) {
        loadContext(null, environment);
    }

    private void loadContext(Class<?> additionalConfiguration, String... environment) {
        this.context = new AnnotationConfigApplicationContext();
        TestPropertyValues.of(environment).applyTo(this.context);
        ConfigurationPropertySources.attach(this.context.getEnvironment());
        this.context.register(SendGridAutoConfiguration.class);
        if (additionalConfiguration != null) {
            this.context.register(additionalConfiguration);
        }
        this.context.refresh();
    }

    @Configuration(proxyBeanMethods = false)
    static class ManualSendGridConfiguration {

        @Bean
        SendGrid sendGrid() {
            return new SendGrid(MY_CUSTOM_API_KEY, true);
        }
    }
}
