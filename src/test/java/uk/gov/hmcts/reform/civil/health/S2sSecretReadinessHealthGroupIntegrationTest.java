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
 * {@code management.endpoint.health.group.readiness.include} entry in application.yaml, and that the
 * indicator can actually drag the readiness GROUP to DOWN (which is what pulls the pod from traffic).
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
        "management.endpoint.health.group.readiness.include=readinessState, s2sSecretReadiness",
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
        // make readinessState UP so the group status reflects our indicator, not the availability default
        AvailabilityChangeEvent.publish(context, ReadinessState.ACCEPTING_TRAFFIC);
    }

    @Test
    void readinessGroupIncludesTheIndicatorAndIsUpWhenS2sTokenGenerates() {
        markContextAcceptingTraffic();
        when(serviceAuthTokenGenerator.generate()).thenReturn("Bearer valid");
        indicator.refresh();

        HealthComponent readiness = healthEndpoint.healthForPath("readiness");

        assertThat(readiness.getStatus()).isEqualTo(Status.UP);
        assertThat(((CompositeHealth) readiness).getComponents())
            .as("readiness group must reference the contributor named in application.yaml")
            .containsKey("s2sSecretReadiness");
    }

    @Test
    void readinessGroupGoesDownWhenS2sTokenGenerationFails() {
        markContextAcceptingTraffic();
        when(serviceAuthTokenGenerator.generate())
            .thenThrow(new IllegalStateException("microservice key is null"));
        indicator.refresh();

        HealthComponent readiness = healthEndpoint.healthForPath("readiness");

        assertThat(readiness.getStatus())
            .as("a DOWN S2S indicator must drag the readiness group DOWN so the pod leaves the Service")
            .isEqualTo(Status.DOWN);
        assertThat(((CompositeHealth) readiness).getComponents()).containsKey("s2sSecretReadiness");
    }
}
