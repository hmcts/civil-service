package uk.gov.hmcts.reform.civil.enums.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum SmallClaimsTimeEstimate {
    @CCD(label = "30 minutes")
    @JsonProperty("THIRTY_MINUTES")
    THIRTY_MINUTES("30 minutes"),
    @CCD(label = "1 hour")
    @JsonProperty("ONE_HOUR")
    ONE_HOUR("One hour"),
    @CCD(label = "1.5 hours")
    @JsonProperty("ONE_AND_HALF_HOUR")
    ONE_AND_HALF_HOUR("One and half hour"),
    @CCD(label = "2 hours")
    @JsonProperty("TWO_HOURS")
    TWO_HOURS("Two hours"),
    @CCD(label = "2.5 hours")
    @JsonProperty("TWO_AND_HALF_HOURS")
    TWO_AND_HALF_HOURS("Two and half hours"),
    @JsonProperty("THREE_HOURS")
    THREE_HOURS("Three hours"),
    @JsonProperty("FOUR_HOURS")
    FOUR_HOURS("Four hours"),
    @JsonProperty("ONE_DAY")
    ONE_DAY("One day"),
    @CCD(label = "Other")
    @JsonProperty("OTHER")
    OTHER("Other");

    private final String label;

    SmallClaimsTimeEstimate(String value) {
        this.label = value;
    }
}
