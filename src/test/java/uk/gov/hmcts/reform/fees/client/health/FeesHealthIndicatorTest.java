package uk.gov.hmcts.reform.fees.client.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import uk.gov.hmcts.reform.fees.client.FeesApi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeesHealthIndicatorTest {

    @Mock
    private FeesApi feesApi;

    @Mock
    private InternalHealth internalHealth;

    private FeesHealthIndicator feesHealthIndicator;

    @BeforeEach
    void setUp() {
        feesHealthIndicator = new FeesHealthIndicator(feesApi);
    }

    @Test
    void shouldReturnHealthUp_whenFeesApiReturnsHealthyStatus() {
        when(feesApi.health()).thenReturn(internalHealth);
        when(internalHealth.getStatus()).thenReturn(Status.UP);

        Health health = feesHealthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void shouldReturnHealthDown_whenFeesApiReturnsDownStatus() {
        when(feesApi.health()).thenReturn(internalHealth);
        when(internalHealth.getStatus()).thenReturn(Status.DOWN);

        Health health = feesHealthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void shouldReturnHealthUp_whenFeesApiReturnsNull() {
        when(feesApi.health()).thenReturn(null);

        Health health = feesHealthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("fees-api", "Available");
    }

    @Test
    void shouldReturnHealthUp_whenInternalHealthStatusIsNull() {
        when(feesApi.health()).thenReturn(internalHealth);
        when(internalHealth.getStatus()).thenReturn(null);

        Health health = feesHealthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("fees-api", "Available");
    }

    @Test
    void shouldReturnHealthDown_whenFeesApiThrowsException() {
        String errorMessage = "Connection refused";
        when(feesApi.health()).thenThrow(new RuntimeException(errorMessage));

        Health health = feesHealthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
