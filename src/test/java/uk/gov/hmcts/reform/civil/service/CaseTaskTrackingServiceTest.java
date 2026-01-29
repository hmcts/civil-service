package uk.gov.hmcts.reform.civil.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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
        final String caseId = "111";
        final String eventType = "serviceBusMessage";
        final String eventName = "NotifyRobotics";
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
    @SuppressWarnings("unchecked")
    void trackCaseTask_additionalPropertiesOverrideBaseKeys_whenDuplicateKeysProvided() {

        Map<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put("caseId", "OVERRIDDEN");
        additionalProperties.put("eventType", "OVERRIDE_TYPE");
        additionalProperties.put("extra", "x");

        String caseId = "333";
        String eventType = "originalType";
        String eventName = "EventWithOverrides";
        caseTaskTrackingService.trackCaseTask(caseId, eventType, eventName, additionalProperties);

        ArgumentCaptor<Map<String, String>> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(telemetryClient).trackEvent(eq(eventName), propertiesCaptor.capture(), isNull());

        Map<String, String> captured = propertiesCaptor.getValue();
        // size should be 3: caseId, eventType, extra (after overrides)
        assertEquals(3, captured.size());
        assertEquals("OVERRIDDEN", captured.get("caseId"));
        assertEquals("OVERRIDE_TYPE", captured.get("eventType"));
        assertEquals("x", captured.get("extra"));
    }

    @Test
    void rememberAndConsumeErrors_roundTrip() {
        String caseId = "444";
        String taskId = "NotifyTask";
        List<String> errors = List.of("e1", "e2");

        caseTaskTrackingService.rememberErrors(caseId, taskId, errors);
        String consumed = caseTaskTrackingService.consumeErrors(caseId, taskId);
        assertEquals("e1 | e2", consumed);

        // ensure consuming again returns null (cleared)
        String again = caseTaskTrackingService.consumeErrors(caseId, taskId);
        assertThat(again).isNull();
    }

    @Test
    void rememberErrors_nullList_doesNotStoreAnything() {
        String caseId = "555";
        String taskId = "T1";

        caseTaskTrackingService.rememberErrors(caseId, taskId, null);
        String consumed = caseTaskTrackingService.consumeErrors(caseId, taskId);
        assertThat(consumed).isNull();
    }

    @Test
    void rememberErrors_emptyList_doesNotStoreAnything() {
        String caseId = "556";
        String taskId = "T2";

        caseTaskTrackingService.rememberErrors(caseId, taskId, List.of());
        String consumed = caseTaskTrackingService.consumeErrors(caseId, taskId);
        assertThat(consumed).isNull();
    }

    @Test
    void rememberErrors_overwritesExisting_forSameCaseAndTask() {
        String caseId = "557";
        String taskId = "T3";

        caseTaskTrackingService.rememberErrors(caseId, taskId, List.of("first"));
        caseTaskTrackingService.rememberErrors(caseId, taskId, List.of("second", "third"));

        String consumed = caseTaskTrackingService.consumeErrors(caseId, taskId);
        assertThat(consumed).isEqualTo("second | third");
    }

    @Test
    void rememberAndConsumeErrors_handlesNullTaskId_keyingAsEmptySuffix() {
        String caseId = "558";
        String taskId = null;

        caseTaskTrackingService.rememberErrors(caseId, taskId, List.of("eA", "eB"));
        String consumed = caseTaskTrackingService.consumeErrors(caseId, taskId);
        assertThat(consumed).isEqualTo("eA | eB");

        // Make sure a different taskId doesn't collide
        caseTaskTrackingService.rememberErrors(caseId, "nonNull", List.of("X"));
        String consumedDifferent = caseTaskTrackingService.consumeErrors(caseId, null);
        assertThat(consumedDifferent).isNull();
        String consumedNonNull = caseTaskTrackingService.consumeErrors(caseId, "nonNull");
        assertThat(consumedNonNull).isEqualTo("X");
    }

    @Test
    void rememberErrors_isolatedByCaseAndTask_combinations() {
        String case1 = "C1";
        String case2 = "C2";
        String taskA = "A";
        String taskB = "B";

        caseTaskTrackingService.rememberErrors(case1, taskA, List.of("e1"));
        caseTaskTrackingService.rememberErrors(case1, taskB, List.of("e2"));
        caseTaskTrackingService.rememberErrors(case2, taskA, List.of("e3"));

        assertThat(caseTaskTrackingService.consumeErrors(case1, taskA)).isEqualTo("e1");
        assertThat(caseTaskTrackingService.consumeErrors(case1, taskB)).isEqualTo("e2");
        assertThat(caseTaskTrackingService.consumeErrors(case2, taskA)).isEqualTo("e3");
        // ensure no leakage
        assertThat(caseTaskTrackingService.consumeErrors(case2, taskB)).isNull();
    }
}
