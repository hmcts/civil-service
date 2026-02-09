package uk.gov.hmcts.reform.fees.client.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Overrides the FeesHealthIndicator from fees-java-client library.
 * The library's health indicator is incompatible with the current Spring Boot version
 * due to API changes in Health.down(Exception) method signature.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "fees", name = "api.url")
@EnableFeignClients(basePackages = "uk.gov.hmcts.reform.fees.client")
public class FeesHealthIndicator {

    @Value("${fees.api.url:}")
    private String feesApiUrl;

    @Bean
    @Primary
    public HealthIndicator feesHealthIndicator() {
        return () -> {
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
                log.error("Error checking fees-api health", ex);
                return Health.down().withDetail("fees-api", ex.getMessage()).build();
            }
        };
    }
}
