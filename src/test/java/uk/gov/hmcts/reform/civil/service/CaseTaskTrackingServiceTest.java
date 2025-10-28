package uk.gov.hmcts.reform.civil.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CaseTaskTrackingServiceTest {

    @Mock
    private TelemetryClient telemetryClient;

    @InjectMocks
    private CaseTaskTrackingService caseTaskTrackingService;

    @Test
    @SuppressWarnings("unchecked")
    void trackCaseTask_withNullAdditionalProperties_shouldOnlyAddCaseAndEventProperties() {
        String caseId = "111";
        String eventType = "serviceBusMessage";
        String eventName = "NotifyRobotics";

        caseTaskTrackingService.trackCaseTask(caseId, eventType, eventName, null);

        ArgumentCaptor<Map<String, String>> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(telemetryClient).trackEvent(eq(eventName), propertiesCaptor.capture(), isNull());

        Map<String, String> capturedProperties = propertiesCaptor.getValue();
        assertEquals(2, capturedProperties.size());
        assertEquals(caseId, capturedProperties.get("caseId"));
        assertEquals(eventType, capturedProperties.get("eventType"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void trackCaseTask_withAdditionalProperties_shouldMergeAllProperties() {
        String caseId = "222";
        String eventType = "update";
        String eventName = "CaseUpdated";

        Map<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put("someKey", "someValue");
        additionalProperties.put("anotherKey", "anotherValue");

        caseTaskTrackingService.trackCaseTask(caseId, eventType, eventName, additionalProperties);

        ArgumentCaptor<Map<String, String>> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(telemetryClient).trackEvent(eq(eventName), propertiesCaptor.capture(), isNull());

        Map<String, String> capturedProperties = propertiesCaptor.getValue();
        assertEquals(4, capturedProperties.size());
        assertEquals(caseId, capturedProperties.get("caseId"));
        assertEquals(eventType, capturedProperties.get("eventType"));
        assertEquals("someValue", capturedProperties.get("someKey"));
        assertEquals("anotherValue", capturedProperties.get("anotherKey"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void trackCaseTask_withAdditionalPropertiesOverridingDefaults_shouldPreferAdditionalValues() {
        String caseId = "333";
        String eventType = "create";
        String eventName = "CaseCreated";

        Map<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put("caseId", "OVERRIDDEN_CASE_ID");
        additionalProperties.put("eventType", "OVERRIDDEN_EVENT_TYPE");
        additionalProperties.put("custom", "value");

        caseTaskTrackingService.trackCaseTask(caseId, eventType, eventName, additionalProperties);

        ArgumentCaptor<Map<String, String>> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(telemetryClient).trackEvent(eq(eventName), propertiesCaptor.capture(), isNull());

        Map<String, String> capturedProperties = propertiesCaptor.getValue();
        // size: 3 -> overridden caseId, overridden eventType, and "custom"
        assertEquals(3, capturedProperties.size());
        assertEquals("OVERRIDDEN_CASE_ID", capturedProperties.get("caseId"));
        assertEquals("OVERRIDDEN_EVENT_TYPE", capturedProperties.get("eventType"));
        assertEquals("value", capturedProperties.get("custom"));
    }
}
