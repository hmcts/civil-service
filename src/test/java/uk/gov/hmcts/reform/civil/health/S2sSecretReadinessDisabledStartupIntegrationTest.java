package uk.gov.hmcts.reform.civil.health;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Regression test for the disabled configuration: with {@code civil.health.s2s.enabled=false} the
 * {@code s2s} health group still references {@code s2sSecretReadiness} and group-membership
 * validation is on (the production default). The application context must still start - which only
 * holds because the indicator bean is always registered and merely reports UP when disabled, rather
 * than being removed by a conditional. If the bean were conditional, this context would fail to
 * start.
 */
@SpringBootTest(
    classes = {
        S2sSecretReadinessHealthIndicator.class,
        S2sSecretReadinessDisabledStartupIntegrationTest.TestConfig.class,
        EndpointAutoConfiguration.class,
        HealthContributorAutoConfiguration.class,
        HealthEndpointAutoConfiguration.class,
    },
    properties = {
        "management.endpoint.health.show-details=always",
        "management.endpoint.health.group.s2s.include=s2sSecretReadiness",
        "management.endpoint.health.validate-group-membership=true",
        "civil.health.s2s.enabled=false",
    }
)
class S2sSecretReadinessDisabledStartupIntegrationTest {

    @Configuration
    static class TestConfig {
        @Bean
        AuthTokenGenerator serviceAuthTokenGenerator() {
            return mock(AuthTokenGenerator.class);
        }
    }

    @Autowired
    private HealthEndpoint healthEndpoint;

    @Test
    void contextStartsAndS2sGroupIsUpWhenTheCheckIsDisabled() {
        // Reaching this assertion proves the context started with the check disabled and the s2s
        // group still referencing the contributor under group-membership validation.
        HealthComponent s2s = healthEndpoint.healthForPath("s2s");

        assertThat(s2s.getStatus()).isEqualTo(Status.UP);
    }
}
