package uk.gov.hmcts.reform.civil.enums.hearing;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryType {
    CASE_TYPE("caseType"),
    CASE_SUBTYPE("caseSubType");

    @JsonValue
    private final String stringValueForQuery;
}
