package uk.gov.hmcts.reform.civil.enums.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum SmallClaimsSdoR2TimeEstimate {

    @JsonProperty("FIFTEEN_MINUTES")
    FIFTEEN_MINUTES("15 minutes"),

    @JsonProperty("THIRTY_MINUTES")
    THIRTY_MINUTES("30 minutes"),

    @JsonProperty("ONE_HOUR")
    ONE_HOUR("One hour"),

    @JsonProperty("OTHER")
    OTHER("Other");

    private final String label;

    SmallClaimsSdoR2TimeEstimate(String value) {
        this.label = value;
    }
}
