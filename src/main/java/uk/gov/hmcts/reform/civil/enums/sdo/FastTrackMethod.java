package uk.gov.hmcts.reform.civil.enums.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum FastTrackMethod {
    @CCD(label = "In Person")
    @JsonProperty("fastTrackMethodInPerson")
    FAST_TRACK_METHOD_IN_PERSON,
    @CCD(label = "Video")
    @JsonProperty("fastTrackMethodVideoConferenceHearing")
    FAST_TRACK_METHOD_VIDEO_CONFERENCE_HEARING,
    @CCD(label = "Telephone")
    @JsonProperty("fastTrackMethodTelephoneHearing")
    FAST_TRACK_METHOD_TELEPHONE_HEARING
}
