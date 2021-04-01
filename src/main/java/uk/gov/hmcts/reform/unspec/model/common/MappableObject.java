package uk.gov.hmcts.reform.unspec.model.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public interface MappableObject {

    default Map<String, Object> toMap(ObjectMapper mapper) {
        return mapper.convertValue(this, new TypeReference<>() {
        });
    }
}
