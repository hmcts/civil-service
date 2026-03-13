package uk.gov.hmcts.reform.civil.model.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface MappableObject {

    default Map<String, Object> toMap(ObjectMapper mapper) {
        Map<String, Object> mapped = mapper.convertValue(this, new TypeReference<>() {
        });
        if (mapped == null) {
            return null;
        }
        removeEmptyUserDetailsRecursively(mapped);
        return mapped;
    }

    @SuppressWarnings("unchecked")
    private static void removeEmptyUserDetailsRecursively(Map<String, Object> map) {
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            Object value = entry.getValue();

            if (value instanceof Map<?, ?> nestedMap) {
                removeEmptyUserDetailsRecursively((Map<String, Object>) nestedMap);
                if (isEmptyUserDetails(entry.getKey(), nestedMap)) {
                    iterator.remove();
                }
            } else if (value instanceof List<?> list) {
                removeEmptyUserDetailsFromList((List<Object>) list);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void removeEmptyUserDetailsFromList(List<Object> list) {
        for (Object item : list) {
            if (item instanceof Map<?, ?> nestedMap) {
                removeEmptyUserDetailsRecursively((Map<String, Object>) nestedMap);
            } else if (item instanceof List<?> nestedList) {
                removeEmptyUserDetailsFromList((List<Object>) nestedList);
            }
        }
    }

    private static boolean isEmptyUserDetails(String key, Map<?, ?> value) {
        return key != null && key.endsWith("UserDetails") && value.isEmpty();
    }
}
