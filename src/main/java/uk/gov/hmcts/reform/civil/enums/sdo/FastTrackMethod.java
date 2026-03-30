package uk.gov.hmcts.reform.civil.enums.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FastTrackMethod {
    @JsonProperty("fastTrackMethodInPerson")
    FAST_TRACK_METHOD_IN_PERSON,
    @JsonProperty("fastTrackMethodVideoConferenceHearing")
    FAST_TRACK_METHOD_VIDEO_CONFERENCE_HEARING,
    @JsonProperty("fastTrackMethodTelephoneHearing")
    FAST_TRACK_METHOD_TELEPHONE_HEARING
}
