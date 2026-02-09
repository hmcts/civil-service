package uk.gov.hmcts.reform.fees.client.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fees.client.FeesApi;

/**
 * Shadows the FeesHealthIndicator from fees-java-client library.
 * This class must be API-compatible with the library's class so that
 * FeesClientAutoConfiguration can instantiate it.
 *
 * <p>The library's health indicator calls feesApi.health() which returns InternalHealth,
 * but the JSON deserialization of the Status field can fail with WireMock responses,
 * causing NullPointerException. This implementation uses RestTemplate directly to
 * avoid deserialization issues.</p>
 */
public class FeesHealthIndicator implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeesHealthIndicator.class);

    private final String feesApiUrl;

    /**
     * Constructor matching the library's signature for API compatibility.
     * The FeesApi parameter provides the base URL but we use RestTemplate directly.
     *
     * @param feesApi the Feign client (used to extract base URL configuration)
     */
    public FeesHealthIndicator(final FeesApi feesApi) {
        // Extract base URL from the FeesApi - we'll construct it from the same property
        // that FeesApi uses. The feesApi instance isn't directly useful since its
        // health() method has the deserialization issue we're avoiding.
        this.feesApiUrl = System.getProperty(
            "fees.api.url",
            System.getenv().getOrDefault("FEES_API_URL", "")
        );
    }

    @Override
    public Health health() {
        if (feesApiUrl == null || feesApiUrl.isEmpty()) {
            return Health.up().withDetail("fees-api", "Not configured").build();
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(feesApiUrl + "/health", String.class);
            return Health.up()
                .withDetail("fees-api", "Available")
                .withDetail("response", response.getBody())
                .build();
        } catch (Exception ex) {
            LOGGER.error("Error on fees client healthcheck", ex);
            return Health.down().withDetail("fees-api", ex.getMessage()).build();
        }
    }
}
