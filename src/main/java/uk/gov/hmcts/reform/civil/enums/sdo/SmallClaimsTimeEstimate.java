package uk.gov.hmcts.reform.civil.enums.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum SmallClaimsTimeEstimate {
    @JsonProperty("THIRTY_MINUTES")
    THIRTY_MINUTES("30 minutes"),
    @JsonProperty("ONE_HOUR")
    ONE_HOUR("One hour"),
    @JsonProperty("ONE_AND_HALF_HOUR")
    ONE_AND_HALF_HOUR("One and half hour"),
    @JsonProperty("TWO_HOURS")
    TWO_HOURS("Two hours"),
    @JsonProperty("TWO_AND_HALF_HOURS")
    TWO_AND_HALF_HOURS("Two and half hours"),
    @JsonProperty("THREE_HOURS")
    THREE_HOURS("Three hours"),
    @JsonProperty("FOUR_HOURS")
    FOUR_HOURS("Four hours"),
    @JsonProperty("ONE_DAY")
    ONE_DAY("One day"),
    @JsonProperty("OTHER")
    OTHER("Other");

    private final String label;

    SmallClaimsTimeEstimate(String value) {
        this.label = value;
    }
}
