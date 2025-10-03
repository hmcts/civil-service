package uk.gov.hmcts.reform.civil.config;

import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

@Configuration
@Profile("integration-test")
public class FlywayIntegrationTestConfiguration {

    private static final String ACCEPT_UNKNOWN_VERSIONS_PROPERTY = "flyway.postgresql.acceptUnknownVersions";

    @Bean
    public FlywayConfigurationCustomizer flywayAcceptUnknownPostgresVersionsCustomizer() {
        System.setProperty(ACCEPT_UNKNOWN_VERSIONS_PROPERTY, "true");
        return configuration -> { /* no-op */ };
    }

    @PreDestroy
    public void clearAcceptUnknownVersionsOverride() {
        System.clearProperty(ACCEPT_UNKNOWN_VERSIONS_PROPERTY);
    }
}
