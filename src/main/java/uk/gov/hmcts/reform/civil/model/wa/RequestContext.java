package uk.gov.hmcts.reform.civil.model.wa;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum RequestContext {

    ALL_WORK("ALL_WORK"),
    AVAILABLE_TASKS("AVAILABLE_TASKS");

    @JsonValue
    private final String id;

    RequestContext(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
