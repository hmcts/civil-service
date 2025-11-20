package uk.gov.hmcts.reform.civil.model.taskmanagement;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SortOrder {

    ASCENDANT("asc"),
    DESCENDANT("desc");

    @JsonValue
    private final String id;

    @Override
    public String toString() {
        return id;
    }
}
