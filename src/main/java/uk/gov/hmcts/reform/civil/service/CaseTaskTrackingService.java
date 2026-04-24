package uk.gov.hmcts.reform.civil.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class CaseTaskTrackingService {

    private final TelemetryService telemetryService;

    private final Map<String, String> recentErrors = new ConcurrentHashMap<>();

    public CaseTaskTrackingService(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    public void trackCaseTask(String caseId,
                              String eventType,
                              String eventName,
                              Map<String, String> additionalProperties) {

        log.info("tracking event for case {} with eventType {} and eventName {}", caseId, eventType, eventType);

        Map<String, String> properties = new HashMap<>();
        properties.put("caseId", caseId);
        properties.put("eventType", eventType);

        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }

        telemetryService.trackEvent(eventName, properties);
    }

    private String key(String caseId, String taskId) {
        return caseId + "::" + (taskId == null ? "" : taskId);
    }

    public void rememberErrors(String caseId, String taskId, List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return;
        }
        String joined = String.join(" | ", errors);
        recentErrors.put(key(caseId, taskId), joined);
    }

    public String consumeErrors(String caseId, String taskId) {
        return recentErrors.remove(key(caseId, taskId));
    }
}
