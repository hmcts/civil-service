package uk.gov.hmcts.reform.civil.enums.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
public enum SmallClaimsSdoR2TimeEstimate {

    @CCD(label = "15 minutes")
    @JsonProperty("FIFTEEN_MINUTES")
    FIFTEEN_MINUTES("15 minutes"),

    @CCD(label = "30 minutes")
    @JsonProperty("THIRTY_MINUTES")
    THIRTY_MINUTES("30 minutes"),

    @CCD(label = "1 hour")
    @JsonProperty("ONE_HOUR")
    ONE_HOUR("One hour"),

    @CCD(label = "Other")
    @JsonProperty("OTHER")
    OTHER("Other");

    private final String label;

    SmallClaimsSdoR2TimeEstimate(String value) {
        this.label = value;
    }
}
