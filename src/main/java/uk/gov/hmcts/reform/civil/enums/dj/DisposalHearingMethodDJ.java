package uk.gov.hmcts.reform.civil.enums.dj;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum DisposalHearingMethodDJ {
    @CCD(label = "In person")
    @JsonProperty("disposalHearingMethodInPerson")
    DISPOSAL_HEARING_METHOD_IN_PERSON,

    @CCD(label = "Video")
    @JsonProperty("disposalHearingMethodVideoConferenceHearing")
    DISPOSAL_HEARING_METHOD_VIDEO_CONFERENCE_HEARING,

    @CCD(label = "Telephone")
    @JsonProperty("disposalHearingMethodTelephoneHearing")
    DISPOSAL_HEARING_METHOD_TELEPHONE_HEARING
}
