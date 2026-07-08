package uk.gov.hmcts.reform.civil.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S2sSecretReadinessHealthIndicatorTest {

    private static final int FAILURE_THRESHOLD = 2;

    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;

    private S2sSecretReadinessHealthIndicator indicator() {
        return new S2sSecretReadinessHealthIndicator(serviceAuthTokenGenerator, FAILURE_THRESHOLD);
    }

    @Test
    void reportsDownBeforeFirstCheck() {
        Health health = indicator().health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("lastError", "S2S readiness not yet verified");
    }

    @Test
    void reportsUpAfterSuccessfulTokenGeneration() {
        when(serviceAuthTokenGenerator.generate()).thenReturn("Bearer eyJ...valid");
        S2sSecretReadinessHealthIndicator indicator = indicator();

        indicator.refresh();
        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("consecutiveFailures", 0);
        assertThat(health.getDetails()).containsKey("lastSuccess");
    }

    @Test
    void reportsDownWhenTokenGenerationThrowsFromTheStart() {
        when(serviceAuthTokenGenerator.generate())
            .thenThrow(new IllegalStateException("microservice key is null"));
        S2sSecretReadinessHealthIndicator indicator = indicator();

        indicator.refresh();
        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("lastError").toString())
            .contains("microservice key is null");
    }

    @Test
    void reportsDownWhenTokenIsBlank() {
        when(serviceAuthTokenGenerator.generate()).thenReturn("   ");
        S2sSecretReadinessHealthIndicator indicator = indicator();

        indicator.refresh();

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void toleratesASingleBlipOnceHealthyButFailsAfterThreshold() {
        when(serviceAuthTokenGenerator.generate())
            .thenReturn("Bearer valid")                       // 1st: success -> UP
            .thenThrow(new RuntimeException("transient s2s 503")) // 2nd: 1 failure -> still UP (blip)
            .thenThrow(new RuntimeException("transient s2s 503")); // 3rd: 2 failures -> DOWN
        S2sSecretReadinessHealthIndicator indicator = indicator();

        indicator.refresh();
        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);

        indicator.refresh();
        assertThat(indicator.health().getStatus())
            .as("one blip within the threshold should not remove a healthy pod from traffic")
            .isEqualTo(Status.UP);

        indicator.refresh();
        assertThat(indicator.health().getStatus())
            .as("consecutive failures reaching the threshold should mark the pod NOT READY")
            .isEqualTo(Status.DOWN);
    }

    @Test
    void recoversToUpAfterAFailureIsFollowedByASuccess() {
        when(serviceAuthTokenGenerator.generate())
            .thenThrow(new RuntimeException("transient s2s 503"))
            .thenReturn("Bearer valid");
        S2sSecretReadinessHealthIndicator indicator = indicator();

        indicator.refresh();
        indicator.refresh();

        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("consecutiveFailures", 0);
    }
}
