package uk.gov.hmcts.reform.fees.client.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import uk.gov.hmcts.reform.civil.client.FeesApiClient;

public class FeesHealthIndicator implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeesHealthIndicator.class);

    private final FeesApiClient feesApiClient;

    public FeesHealthIndicator(final FeesApiClient feesApiClient) {
        this.feesApiClient = feesApiClient;
    }

    @Override
    public Health health() {
        try {
            InternalHealth internalHealth = this.feesApiClient.health();
            return new Health.Builder(internalHealth.getStatus()).build();
        } catch (Exception ex) {
            LOGGER.error("Error on fees client healthcheck", ex);
            return Health.down(ex).build();
        }
    }
}
