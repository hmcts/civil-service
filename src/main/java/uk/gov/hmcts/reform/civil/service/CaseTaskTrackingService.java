package uk.gov.hmcts.reform.civil.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CaseTaskTrackingService {

    private final TelemetryClient telemetryClient;

    public void trackCaseTask(String caseId,
                              String eventType,
                              String eventName,
                              Map<String, String> additionalProperties) {

        Map<String, String> properties = new HashMap<>();
        properties.put("caseId", caseId);
        properties.put("eventType", eventType);

        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }

        telemetryClient.trackEvent(eventName, properties, null);
    }
}
