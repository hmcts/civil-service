package uk.gov.hmcts.reform.civil.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelemetryServiceTest {

    @Mock
    private TelemetryClient telemetryClient;

    @Mock
    private ObjectProvider<TelemetryClient> telemetryClientProvider;

    private TelemetryService telemetryService;

    @BeforeEach
    void setUp() {
        telemetryService = new TelemetryService(telemetryClientProvider);
    }

    @Test
    void shouldTrackEvent_whenTelemetryClientIsPresent() {
        // Given
        String eventName = "testEvent";
        Map<String, String> properties = Map.of("key", "value");
        when(telemetryClientProvider.getIfAvailable()).thenReturn(telemetryClient);

        // When
        telemetryService.trackEvent(eventName, properties);

        // Then
        verify(telemetryClient).trackEvent(eventName, properties, null);
    }

    @Test
    void shouldNotThrowException_whenTelemetryClientIsNull() {
        // Given
        when(telemetryClientProvider.getIfAvailable()).thenReturn(null);
        String eventName = "testEvent";
        Map<String, String> properties = Map.of("key", "value");

        // When
        telemetryService.trackEvent(eventName, properties);

        // Then
        verifyNoInteractions(telemetryClient);
    }
}
