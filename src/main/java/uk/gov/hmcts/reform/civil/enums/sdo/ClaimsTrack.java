package uk.gov.hmcts.reform.civil.enums.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ClaimsTrack {
    @JsonProperty("smallClaimsTrack")
    SMALL_CLAIMS_TRACK,
    @JsonProperty("fastTrack")
    FAST_TRACK
}
