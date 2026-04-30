package uk.gov.hmcts.reform.civil.enums.dj;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DisposalHearingMethodDJ {
    @JsonProperty("disposalHearingMethodInPerson")
    DISPOSAL_HEARING_METHOD_IN_PERSON,

    @JsonProperty("disposalHearingMethodVideoConferenceHearing")
    DISPOSAL_HEARING_METHOD_VIDEO_CONFERENCE_HEARING,

    @JsonProperty("disposalHearingMethodTelephoneHearing")
    DISPOSAL_HEARING_METHOD_TELEPHONE_HEARING
}
