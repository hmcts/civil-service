package uk.gov.hmcts.reform.unspec.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum YesOrNo {
    @JsonProperty("Yes")
    YES,
    @JsonProperty("No")
    NO
}
