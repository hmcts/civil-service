package uk.gov.hmcts.reform.civil.model.taskmanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@JsonFormat(shape = JsonFormat.Shape.STRING)
@AllArgsConstructor
public enum RequestContext {

    ALL_WORK("ALL_WORK"),
    AVAILABLE_TASKS("AVAILABLE_TASKS");

    @JsonValue
    private final String id;

    @Override
    public String toString() {
        return id;
    }
}
