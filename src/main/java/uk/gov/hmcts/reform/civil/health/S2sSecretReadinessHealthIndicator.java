package uk.gov.hmcts.reform.civil.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Diagnostic health indicator that verifies civil-service can actually USE its mounted secret
 * material, rather than only that a secret file is present.
 *
 * <p>It does this by minting a service-to-service (S2S) token via {@link AuthTokenGenerator},
 * which relies on the {@code idam.s2s-auth.totp_secret} mounted from Key Vault. If that secret
 * failed to mount or is stale (the failure mode behind the 30 Jun P1, incident 179051 /
 * DTSCCI-5762), token generation fails and this indicator reports DOWN. It is exposed as a
 * dedicated {@code /health/s2s} group for alerting and dashboards, and contributes to the overall
 * {@code /health}, but it deliberately does NOT sit in the Kubernetes {@code readiness} group.
 * Gating readiness on an external S2S call would let a transient S2S blip pull every pod out of
 * rotation at once (a fleet-wide outage), so this is an observability signal to alert on, not a
 * traffic gate.</p>
 *
 * <p>The indicator starts UP (optimistic) so a freshly deployed pod is not reported DOWN during
 * the start-up window before its first scheduled verification. It only flips to DOWN after
 * {@code failure-threshold} consecutive real failures, and recovers on the next success.</p>
 *
 * <p>The actual S2S call is made on a schedule and its outcome cached, so any health poll only
 * reads in-memory state and never hammers the S2S service. The CSI driver re-mounts secrets on a
 * ~9 minute rotation, so this check is periodic (not start-up only) to catch a pod that goes
 * stale after a healthy start.</p>
 *
 * <p>The bean is always registered (so the {@code s2s} health group membership stays valid and the
 * application starts in every configuration). When {@code civil.health.s2s.enabled} is false the
 * check is inert: it reports UP and skips the scheduled S2S call, rather than removing the bean.</p>
 */
@Slf4j
@Component
public class S2sSecretReadinessHealthIndicator implements HealthIndicator {

    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final boolean enabled;
    private final int failureThreshold;
    private final AtomicReference<State> state = new AtomicReference<>(State.optimisticStartup());

    public S2sSecretReadinessHealthIndicator(
        AuthTokenGenerator serviceAuthTokenGenerator,
        @Value("${civil.health.s2s.enabled:true}") boolean enabled,
        @Value("${civil.health.s2s.failure-threshold:2}") int failureThreshold
    ) {
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.enabled = enabled;
        this.failureThreshold = failureThreshold;
    }

    @Override
    public Health health() {
        if (!enabled) {
            return Health.up().withDetail("disabled", true).build();
        }
        State current = state.get();
        Health.Builder builder = current.healthy ? Health.up() : Health.down();
        builder.withDetail("consecutiveFailures", current.consecutiveFailures);
        if (current.lastSuccess != null) {
            builder.withDetail("lastSuccess", current.lastSuccess.toString());
        }
        if (current.lastAttempt != null) {
            builder.withDetail("lastAttempt", current.lastAttempt.toString());
        }
        if (current.lastError != null) {
            builder.withDetail("lastError", current.lastError);
        }
        return builder.build();
    }

    /**
     * Periodically attempts to mint an S2S token and records the outcome. Uses fixed-delay so a
     * slow/hanging S2S call cannot overlap with the next attempt.
     */
    @Scheduled(
        initialDelayString = "${civil.health.s2s.initial-delay-ms:15000}",
        fixedDelayString = "${civil.health.s2s.check-interval-ms:120000}"
    )
    void refresh() {
        if (!enabled) {
            return;
        }
        try {
            String token = serviceAuthTokenGenerator.generate();
            if (token == null || token.isBlank()) {
                recordFailure("S2S token generator returned an empty token");
            } else {
                recordSuccess();
            }
        } catch (Exception ex) {
            // Any exception here means we could not obtain a valid S2S token (bad/stale secret,
            // or S2S service unreachable). Surface it as DOWN for alerting.
            recordFailure(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private void recordSuccess() {
        Instant now = Instant.now();
        state.updateAndGet(prev -> {
            if (!prev.healthy) {
                log.info("S2S secret check recovered; token generation succeeded again");
            }
            return new State(true, 0, now, now, null);
        });
    }

    private void recordFailure(String error) {
        state.updateAndGet(prev -> {
            int consecutive = prev.consecutiveFailures + 1;
            // Tolerate transient blips: only report DOWN once consecutive failures reach the threshold.
            boolean healthy = prev.healthy && consecutive < failureThreshold;
            if (prev.healthy && !healthy) {
                log.error("S2S secret check failed {} consecutive times (threshold {}); "
                    + "reporting DOWN. Last error: {}", consecutive, failureThreshold, error);
            } else if (!healthy) {
                log.warn("S2S secret check still failing (attempt {}): {}", consecutive, error);
            } else {
                log.warn("S2S secret check blip {} (still within threshold {}): {}",
                    consecutive, failureThreshold, error);
            }
            return new State(healthy, consecutive, prev.lastSuccess, Instant.now(), error);
        });
    }

    private record State(
        boolean healthy,
        int consecutiveFailures,
        Instant lastSuccess,
        Instant lastAttempt,
        String lastError
    ) {
        static State optimisticStartup() {
            return new State(true, 0, null, null, null);
        }
    }
}
