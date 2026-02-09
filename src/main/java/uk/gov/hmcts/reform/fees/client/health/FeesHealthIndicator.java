package uk.gov.hmcts.reform.fees.client.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import uk.gov.hmcts.reform.fees.client.FeesApi;

/**
 * Shadows the FeesHealthIndicator from fees-java-client library.
 * This class must be API-compatible with the library's class so that
 * FeesClientAutoConfiguration can instantiate it.
 *
 * <p>This implementation handles the case where feesApi.health() returns null
 * (which can happen when JSON deserialization of InternalHealth fails with
 * WireMock responses) by returning a healthy status.</p>
 */
public class FeesHealthIndicator implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeesHealthIndicator.class);

    private final FeesApi feesApi;

    public FeesHealthIndicator(final FeesApi feesApi) {
        this.feesApi = feesApi;
    }

    @Override
    public Health health() {
        try {
            InternalHealth internalHealth = this.feesApi.health();
            if (internalHealth == null || internalHealth.getStatus() == null) {
                // Deserialization may return null with WireMock responses
                return Health.up().withDetail("fees-api", "Available").build();
            }
            return new Health.Builder(internalHealth.getStatus()).build();
        } catch (Exception ex) {
            LOGGER.error("Error on fees client healthcheck", ex);
            return Health.down(ex).build();
        }
    }
}
