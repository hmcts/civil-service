package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.fees.client.health.FeesHealthIndicator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeesHealthIndicatorTest {

    private FeesHealthIndicator feesHealthIndicatorOverride;

    @BeforeEach
    void setUp() {
        feesHealthIndicatorOverride = new FeesHealthIndicator();
    }

    @Test
    void shouldReturnHealthUp_whenFeesApiUrlIsNull() {
        ReflectionTestUtils.setField(feesHealthIndicatorOverride, "feesApiUrl", null);

        HealthIndicator healthIndicator = feesHealthIndicatorOverride.feesHealthIndicator();
        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("fees-api", "Not configured");
    }

    @Test
    void shouldReturnHealthUp_whenFeesApiUrlIsEmpty() {
        ReflectionTestUtils.setField(feesHealthIndicatorOverride, "feesApiUrl", "");

        HealthIndicator healthIndicator = feesHealthIndicatorOverride.feesHealthIndicator();
        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("fees-api", "Not configured");
    }

    @Test
    void shouldReturnHealthUp_whenFeesApiHealthEndpointReturnsSuccessfully() {
        String feesApiUrl = "http://localhost:6666";
        String healthResponse = "{\"status\":\"UP\"}";
        ReflectionTestUtils.setField(feesHealthIndicatorOverride, "feesApiUrl", feesApiUrl);

        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(
            RestTemplate.class,
            (mock, context) -> when(mock.getForEntity(eq(feesApiUrl + "/health"), eq(String.class)))
                .thenReturn(new ResponseEntity<>(healthResponse, HttpStatus.OK))
        )) {

            HealthIndicator healthIndicator = feesHealthIndicatorOverride.feesHealthIndicator();
            Health health = healthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsEntry("fees-api", "Available");
            assertThat(health.getDetails()).containsEntry("response", healthResponse);
        }
    }

    @Test
    void shouldReturnHealthDown_whenFeesApiHealthEndpointThrowsException() {
        String feesApiUrl = "http://localhost:6666";
        String errorMessage = "Connection refused";
        ReflectionTestUtils.setField(feesHealthIndicatorOverride, "feesApiUrl", feesApiUrl);

        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(
            RestTemplate.class,
            (mock, context) -> when(mock.getForEntity(eq(feesApiUrl + "/health"), eq(String.class)))
                .thenThrow(new RestClientException(errorMessage))
        )) {

            HealthIndicator healthIndicator = feesHealthIndicatorOverride.feesHealthIndicator();
            Health health = healthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsEntry("fees-api", errorMessage);
        }
    }

    @Test
    void shouldReturnHealthDown_whenFeesApiReturnsNon2xxStatus() {
        String feesApiUrl = "http://localhost:6666";
        ReflectionTestUtils.setField(feesHealthIndicatorOverride, "feesApiUrl", feesApiUrl);

        try (MockedConstruction<RestTemplate> mockedRestTemplate = mockConstruction(
            RestTemplate.class,
            (mock, context) -> when(mock.getForEntity(eq(feesApiUrl + "/health"), eq(String.class)))
                .thenThrow(new RestClientException("503 Service Unavailable"))
        )) {

            HealthIndicator healthIndicator = feesHealthIndicatorOverride.feesHealthIndicator();
            Health health = healthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails().get("fees-api")).asString().contains("503");
        }
    }
}
