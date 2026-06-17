package uk.gov.hmcts.reform.civil.service.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.TelemetryService;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CaseUserRolesCacheMetrics {

    private static final String EVENT_PREFIX = "cache.case_user_roles.";

    private final TelemetryService telemetryService;

    public void recordHit(String caseId, String store) {
        telemetryService.trackEvent(EVENT_PREFIX + "hit", Map.of(
            "caseId", caseId,
            "cacheStore", store
        ));
    }

    public void recordMiss(String caseId) {
        telemetryService.trackEvent(EVENT_PREFIX + "miss", Map.of("caseId", caseId));
    }

    public void recordEviction(String caseId, String reason) {
        telemetryService.trackEvent(EVENT_PREFIX + "eviction", Map.of(
            "caseId", caseId,
            "reason", reason
        ));
    }

    public void recordError(String caseId, String errorType) {
        Map<String, String> props = new HashMap<>();
        props.put("caseId", caseId);
        props.put("errorType", errorType);
        telemetryService.trackEvent(EVENT_PREFIX + "error", props);
    }

    public void recordDisabled() {
        telemetryService.trackEvent(EVENT_PREFIX + "disabled", Map.of());
    }

    public void recordNegative(String caseId) {
        telemetryService.trackEvent(EVENT_PREFIX + "negative", Map.of("caseId", caseId));
    }

    public void recordFallback(String caseId) {
        telemetryService.trackEvent(EVENT_PREFIX + "fallback", Map.of("caseId", caseId));
    }
}
