package uk.gov.hmcts.reform.civil.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CaseTaskTrackingServiceTest {

    @Mock
    private TelemetryClient telemetryClient;

    @Mock
    private ObjectProvider<TelemetryClient> telemetryClientProvider;

    @InjectMocks
    private CaseTaskTrackingService caseTaskTrackingService;

    @Test
    @SuppressWarnings("unchecked")
    void trackCaseTask_withNullAdditionalProperties_shouldOnlyAddCaseAndEventProperties() {
        when(telemetryClientProvider.getIfAvailable()).thenReturn(telemetryClient);
        String caseId = "111";
        String eventType = "serviceBusMessage";
        String eventName = "NotifyRobotics";
        Map<String, String> additionalProperties = null;

        caseTaskTrackingService.trackCaseTask(caseId, eventType, eventName, additionalProperties);

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
        when(telemetryClientProvider.getIfAvailable()).thenReturn(telemetryClient);
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
    void trackCaseTask_shouldSkipWhenTelemetryClientUnavailable() {
        when(telemetryClientProvider.getIfAvailable()).thenReturn(null);

        caseTaskTrackingService.trackCaseTask("333", "type", "name", null);

        verifyNoInteractions(telemetryClient);
    }
}
