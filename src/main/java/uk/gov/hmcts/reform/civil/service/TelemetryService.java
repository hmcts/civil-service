package uk.gov.hmcts.reform.civil.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryService {

    private final ObjectProvider<TelemetryClient> telemetryClientProvider;

    public void trackEvent(String eventName, Map<String, String> properties) {
        TelemetryClient telemetryClient = telemetryClientProvider.getIfAvailable();
        if (telemetryClient == null) {
            log.debug("TelemetryClient not available, skipping event: {}", eventName);
            return;
        }
        telemetryClient.trackEvent(eventName, properties, null);
    }
}
