package uk.gov.hmcts.reform.civil.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum RestrictToCafcassHmcts {
    @JsonProperty("restrictToGroup")
    restrictToGroup("Yes - restrict to this group");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static RestrictToCafcassHmcts getValue(String key) {
        return RestrictToCafcassHmcts.valueOf(key);
    }
}
