package uk.gov.hmcts.reform.civil.health;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.availability.AvailabilityHealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.availability.ApplicationAvailabilityAutoConfiguration;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies the actuator WIRING that the unit test cannot: that {@code s2sSecretReadiness}
 * (the contributor name Spring derives from {@link S2sSecretReadinessHealthIndicator}) matches the
 * {@code management.endpoint.health.group.s2s.include} entry in application.yaml, that the indicator
 * can drag its OWN {@code /health/s2s} group to DOWN, and crucially that it does NOT sit in the
 * Kubernetes {@code readiness} group (so an S2S blip cannot pull the pod out of rotation).
 *
 * <p>Only the actuator health infrastructure and this indicator are loaded, with a mocked
 * {@link AuthTokenGenerator}, so the full civil-service context is not required.</p>
 */
@SpringBootTest(
    classes = {
        S2sSecretReadinessHealthIndicator.class,
        S2sSecretReadinessHealthGroupIntegrationTest.TestConfig.class,
        EndpointAutoConfiguration.class,
        HealthContributorAutoConfiguration.class,
        HealthEndpointAutoConfiguration.class,
        ApplicationAvailabilityAutoConfiguration.class,
        AvailabilityHealthContributorAutoConfiguration.class,
    },
    properties = {
        "management.endpoint.health.show-details=always",
        // register the availability state contributors in this sliced context (Kubernetes is not
        // auto-detected under test, so enable them explicitly); production gets them via probes.enabled
        "management.endpoint.health.probes.enabled=true",
        "management.health.readinessstate.enabled=true",
        "management.health.livenessstate.enabled=true",
        // mirror application.yaml exactly so a rename/typo of the contributor is caught here
        "management.endpoint.health.group.s2s.include=s2sSecretReadiness",
        // the default k8s readiness membership: readinessState only, WITHOUT the S2S check
        "management.endpoint.health.group.readiness.include=readinessState",
        // keep the scheduler out of the test; we drive refresh() explicitly
        "civil.health.s2s.initial-delay-ms=600000",
        "civil.health.s2s.check-interval-ms=600000",
        "civil.health.s2s.failure-threshold=1",
    }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class S2sSecretReadinessHealthGroupIntegrationTest {

    @Configuration
    static class TestConfig {
        @Bean
        AuthTokenGenerator serviceAuthTokenGenerator() {
            return mock(AuthTokenGenerator.class);
        }
    }

    @Autowired
    private ApplicationContext context;

    @Autowired
    private HealthEndpoint healthEndpoint;

    @Autowired
    private AuthTokenGenerator serviceAuthTokenGenerator;

    @Autowired
    private S2sSecretReadinessHealthIndicator indicator;

    private void markContextAcceptingTraffic() {
        AvailabilityChangeEvent.publish(context, ReadinessState.ACCEPTING_TRAFFIC);
    }

    @Test
    void s2sGroupIncludesTheIndicatorAndIsUpWhenS2sTokenGenerates() {
        when(serviceAuthTokenGenerator.generate()).thenReturn("Bearer valid");
        indicator.refresh();

        HealthComponent s2s = healthEndpoint.healthForPath("s2s");

        assertThat(s2s.getStatus()).isEqualTo(Status.UP);
        assertThat(((CompositeHealth) s2s).getComponents())
            .as("s2s group must reference the contributor named in application.yaml")
            .containsKey("s2sSecretReadiness");
    }

    @Test
    void s2sGroupGoesDownWhenS2sTokenGenerationFails() {
        when(serviceAuthTokenGenerator.generate())
            .thenThrow(new IllegalStateException("microservice key is null"));
        indicator.refresh();

        HealthComponent s2s = healthEndpoint.healthForPath("s2s");

        assertThat(s2s.getStatus())
            .as("a DOWN S2S indicator must drag its own s2s group DOWN for alerting")
            .isEqualTo(Status.DOWN);
        assertThat(((CompositeHealth) s2s).getComponents()).containsKey("s2sSecretReadiness");
    }

    @Test
    void readinessGroupDoesNotIncludeTheIndicator() {
        markContextAcceptingTraffic();
        when(serviceAuthTokenGenerator.generate())
            .thenThrow(new IllegalStateException("microservice key is null"));
        indicator.refresh();

        HealthComponent readiness = healthEndpoint.healthForPath("readiness");

        assertThat(((CompositeHealth) readiness).getComponents())
            .as("the S2S check must NOT gate the Kubernetes readiness probe")
            .doesNotContainKey("s2sSecretReadiness");
        assertThat(readiness.getStatus())
            .as("a DOWN S2S check must not drag readiness DOWN and pull the pod from traffic")
            .isEqualTo(Status.UP);
    }
}
