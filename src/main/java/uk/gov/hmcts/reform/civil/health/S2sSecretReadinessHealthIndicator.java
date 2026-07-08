package uk.gov.hmcts.reform.civil.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Readiness health indicator that verifies civil-service can actually USE its mounted secret
 * material, rather than only that a secret file is present.
 *
 * <p>It does this by minting a service-to-service (S2S) token via {@link AuthTokenGenerator},
 * which relies on the {@code idam.s2s-auth.totp_secret} mounted from Key Vault. If that secret
 * failed to mount or is stale (the failure mode behind the 30 Jun P1, incident 179051 /
 * DTSCCI-5762), token generation fails, this indicator reports DOWN, and the pod is pulled from
 * the readiness group so it stops receiving CCD callbacks instead of rejecting them with 403.</p>
 *
 * <p>The actual S2S call is made on a schedule and its outcome cached, so the frequent Kubernetes
 * readiness poll only reads in-memory state and never hammers the S2S service. The CSI driver
 * re-mounts secrets on a ~9 minute rotation, so this check is periodic (not start-up only) to
 * catch a pod that goes stale after a healthy start.</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "civil.health.s2s", name = "enabled", havingValue = "true", matchIfMissing = true)
public class S2sSecretReadinessHealthIndicator implements HealthIndicator {

    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final int failureThreshold;
    private final AtomicReference<State> state = new AtomicReference<>(State.notYetVerified());

    public S2sSecretReadinessHealthIndicator(
        AuthTokenGenerator serviceAuthTokenGenerator,
        @Value("${civil.health.s2s.failure-threshold:2}") int failureThreshold
    ) {
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.failureThreshold = failureThreshold;
    }

    @Override
    public Health health() {
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
        try {
            String token = serviceAuthTokenGenerator.generate();
            if (token == null || token.isBlank()) {
                recordFailure("S2S token generator returned an empty token");
            } else {
                recordSuccess();
            }
        } catch (Exception ex) {
            // Any exception here means we could not obtain a valid S2S token (bad/stale secret,
            // or S2S service unreachable). Either way the pod cannot serve CCD callbacks reliably.
            recordFailure(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private void recordSuccess() {
        Instant now = Instant.now();
        state.updateAndGet(prev -> {
            if (!prev.healthy) {
                log.info("S2S readiness check recovered; pod is READY to receive traffic");
            }
            return new State(true, 0, now, now, null);
        });
    }

    private void recordFailure(String error) {
        state.updateAndGet(prev -> {
            int consecutive = prev.consecutiveFailures + 1;
            // Tolerate a transient blip only once we have been healthy; stay DOWN otherwise
            // (including before the first successful verification at start-up).
            boolean healthy = prev.healthy && consecutive < failureThreshold;
            if (prev.healthy && !healthy) {
                log.error("S2S readiness check failed {} consecutive times (threshold {}); "
                    + "marking pod NOT READY. Last error: {}", consecutive, failureThreshold, error);
            } else if (!healthy) {
                log.warn("S2S readiness check failing (attempt {}, not yet verified/still down): {}",
                    consecutive, error);
            } else {
                log.warn("S2S readiness check blip {} (still within threshold {}): {}",
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
        static State notYetVerified() {
            return new State(false, 0, null, null, "S2S readiness not yet verified");
        }
    }
}
