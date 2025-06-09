package uk.gov.hmcts.reform.civil.model.wa;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SearchParameterKey {

    USER("user"),
    JURISDICTION("jurisdiction"),
    CASE_ID("case_id");

    @JsonValue
    private final String id;

    SearchParameterKey(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

    public String value() {
        return id;
    }
}
