package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class CaseDataContentConverter {

    private CaseDataContentConverter() {

    }

    public static CaseDataContent caseDataContentFromStartEventResponse(
        StartEventResponse startEventResponse, Map<String, Object> contentModified) {
        var payload = new HashMap<>(startEventResponse.getCaseDetails().getData());
        updateMap(payload, contentModified);

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(payload)
            .build();
    }

    private static void updateMap(Map<String, Object> originalContent, Map<String, Object> modifiedContent) {
        modifiedContent.forEach((key, value) -> {
            if (originalContent.containsKey(key)) {
                Object existingValue = originalContent.get(key);
                if (existingValue instanceof Map && value instanceof Map) {
                    updateMap((Map<String, Object>) existingValue, (Map<String, Object>) value);
                } else {
                    originalContent.put(key, value);
                }
            } else {
                originalContent.put(key, value);
            }
        });
    }
}
