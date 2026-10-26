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
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class S2sSecretReadinessHealthIndicatorTest {

    private static final int FAILURE_THRESHOLD = 2;

    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;

    private S2sSecretReadinessHealthIndicator indicator() {
        return new S2sSecretReadinessHealthIndicator(serviceAuthTokenGenerator, true, FAILURE_THRESHOLD);
    }

    private S2sSecretReadinessHealthIndicator disabledIndicator() {
        return new S2sSecretReadinessHealthIndicator(serviceAuthTokenGenerator, false, FAILURE_THRESHOLD);
    }

    @Test
    void reportsUpBeforeFirstCheck() {
        Health health = indicator().health();

        assertThat(health.getStatus())
            .as("starts optimistic so a freshly deployed pod is not reported DOWN before its first check")
            .isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("consecutiveFailures", 0);
        assertThat(health.getDetails()).doesNotContainKey("lastError");
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
    void staysUpOnASingleFailureFromStartup() {
        when(serviceAuthTokenGenerator.generate())
            .thenThrow(new IllegalStateException("microservice key is null"));
        S2sSecretReadinessHealthIndicator indicator = indicator();

        indicator.refresh();
        Health health = indicator.health();

        assertThat(health.getStatus())
            .as("a single failure within the threshold must not report DOWN")
            .isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("consecutiveFailures", 1);
        assertThat(health.getDetails().get("lastError").toString())
            .contains("microservice key is null");
    }

    @Test
    void reportsDownAfterThresholdConsecutiveFailures() {
        when(serviceAuthTokenGenerator.generate())
            .thenThrow(new IllegalStateException("microservice key is null"));
        S2sSecretReadinessHealthIndicator indicator = indicator();

        indicator.refresh();
        indicator.refresh();
        Health health = indicator.health();

        assertThat(health.getStatus())
            .as("consecutive failures reaching the threshold should report DOWN")
            .isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("lastError").toString())
            .contains("microservice key is null");
    }

    @Test
    void reportsDownAfterThresholdBlankTokens() {
        when(serviceAuthTokenGenerator.generate()).thenReturn("   ");
        S2sSecretReadinessHealthIndicator indicator = indicator();

        indicator.refresh();
        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);

        indicator.refresh();
        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void toleratesASingleBlipAfterASuccessButFailsAfterThreshold() {
        when(serviceAuthTokenGenerator.generate())
            .thenReturn("Bearer valid")                          // 1st: success -> UP
            .thenThrow(new RuntimeException("transient s2s 503")) // 2nd: 1 failure -> still UP (blip)
            .thenThrow(new RuntimeException("transient s2s 503")); // 3rd: 2 failures -> DOWN
        S2sSecretReadinessHealthIndicator indicator = indicator();

        indicator.refresh();
        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);

        indicator.refresh();
        assertThat(indicator.health().getStatus())
            .as("one blip within the threshold should not report DOWN")
            .isEqualTo(Status.UP);

        indicator.refresh();
        assertThat(indicator.health().getStatus())
            .as("consecutive failures reaching the threshold should report DOWN")
            .isEqualTo(Status.DOWN);
    }

    @Test
    void recoversToUpAfterFailuresAreFollowedByASuccess() {
        when(serviceAuthTokenGenerator.generate())
            .thenThrow(new RuntimeException("transient s2s 503"))
            .thenThrow(new RuntimeException("transient s2s 503"))
            .thenReturn("Bearer valid");
        S2sSecretReadinessHealthIndicator indicator = indicator();

        indicator.refresh();
        indicator.refresh();
        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);

        indicator.refresh();
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("consecutiveFailures", 0);
    }

    @Test
    void whenDisabledReportsUpAndNeverCallsS2s() {
        S2sSecretReadinessHealthIndicator indicator = disabledIndicator();

        // refresh must be inert when disabled, even if it were somehow scheduled
        indicator.refresh();
        Health health = indicator.health();

        assertThat(health.getStatus())
            .as("a disabled check must report UP so the s2s health group stays valid")
            .isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("disabled", true);
        verifyNoInteractions(serviceAuthTokenGenerator);
    }
}
