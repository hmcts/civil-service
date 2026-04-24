package uk.gov.hmcts.reform.civil.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryService {

    private final TelemetryClient telemetryClient;

    public void trackEvent(String eventName, Map<String, String> properties) {
        if (telemetryClient != null) {
            telemetryClient.trackEvent(eventName, properties, null);
        } else {
            log.debug("TelemetryClient not available, skipping event: {}", eventName);
        }
    }
}
