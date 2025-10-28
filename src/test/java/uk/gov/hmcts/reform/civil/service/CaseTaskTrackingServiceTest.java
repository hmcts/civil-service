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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CaseTaskTrackingServiceTest {

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
    void rememberAndConsumeErrors_roundTrip() {
        String caseId = "333";
        String taskId = "NotifyTask";
        List<String> errors = List.of("e1", "e2");

        caseTaskTrackingService.rememberErrors(caseId, taskId, errors);
        String consumed = caseTaskTrackingService.consumeErrors(caseId, taskId);
        assertEquals("e1 | e2", consumed);

        // ensure consuming again returns null (cleared)
        String again = caseTaskTrackingService.consumeErrors(caseId, taskId);
        org.assertj.core.api.Assertions.assertThat(again).isNull();
    }
}
