package uk.gov.hmcts.reform.civil.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TelemetryServiceTest {

    @Mock
    private TelemetryClient telemetryClient;

    private TelemetryService telemetryService;

    @BeforeEach
    void setUp() {
        telemetryService = new TelemetryService(telemetryClient);
    }

    @Test
    void shouldTrackEvent_whenTelemetryClientIsPresent() {
        // Given
        String eventName = "testEvent";
        Map<String, String> properties = Map.of("key", "value");

        // When
        telemetryService.trackEvent(eventName, properties);

        // Then
        verify(telemetryClient).trackEvent(eventName, properties, null);
    }

    @Test
    void shouldNotThrowException_whenTelemetryClientIsNull() {
        // Given
        TelemetryService telemetryServiceWithNull = new TelemetryService(null);
        String eventName = "testEvent";
        Map<String, String> properties = Map.of("key", "value");

        // When
        telemetryServiceWithNull.trackEvent(eventName, properties);

        // Then
        verifyNoInteractions(telemetryClient);
    }
}
