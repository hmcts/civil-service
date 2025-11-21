package uk.gov.hmcts.reform.civil.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class CaseTaskTrackingService {

    private final ObjectProvider<TelemetryClient> telemetryClientProvider;

    public void trackCaseTask(String caseId,
                              String eventType,
                              String eventName,
                              Map<String, String> additionalProperties) {

        TelemetryClient telemetryClient = telemetryClientProvider.getIfAvailable();
        if (telemetryClient == null) {
            log.debug("Telemetry client not available, skipping tracking for case {}", caseId);
            return;
        }

        log.info("tracking event for case {} with eventType {} and eventName {}", caseId, eventType, eventType);

        Map<String, String> properties = new HashMap<>();
        properties.put("caseId", caseId);
        properties.put("eventType", eventType);

        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }

        telemetryClient.trackEvent(eventName, properties, null);
    }
}
