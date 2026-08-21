package uk.gov.hmcts.reform.civil.enums.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum ClaimsTrack {
    @CCD(label = "Small Claims Track")
    @JsonProperty("smallClaimsTrack")
    SMALL_CLAIMS_TRACK,
    @CCD(label = "Fast Track")
    @JsonProperty("fastTrack")
    FAST_TRACK
}
