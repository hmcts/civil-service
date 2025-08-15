package uk.gov.hmcts.reform.civil.model.taskmanagement;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

import static java.util.Arrays.stream;

@AllArgsConstructor
public enum SearchOperator {

    IN("IN"),
    CONTEXT("CONTEXT"),
    BOOLEAN("BOOLEAN"),
    BETWEEN("BETWEEN"),
    BEFORE("BEFORE"),
    AFTER("AFTER");

    @JsonValue
    private String value;

    public static SearchOperator from(String value) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(value + " is an unsupported operator"));
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
