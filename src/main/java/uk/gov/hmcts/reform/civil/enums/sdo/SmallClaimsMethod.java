package uk.gov.hmcts.reform.civil.enums.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum SmallClaimsMethod {
    @CCD(label = "In Person")
    @JsonProperty("smallClaimsMethodInPerson")
    SMALL_CLAIMS_METHOD_IN_PERSON,
    @CCD(label = "Video")
    @JsonProperty("smallClaimsMethodVideoConferenceHearing")
    SMALL_CLAIMS_METHOD_VIDEO_CONFERENCE_HEARING,
    @CCD(label = "Telephone")
    @JsonProperty("smallClaimsMethodTelephoneHearing")
    SMALL_CLAIMS_METHOD_TELEPHONE_HEARING
}
