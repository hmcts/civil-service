package uk.gov.hmcts.reform.civil.enums.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.sdk.api.CCD;

public enum DisposalHearingMethod {

    @CCD(label = "In Person")
    @JsonProperty("disposalHearingMethodInPerson")
    DISPOSAL_HEARING_METHOD_IN_PERSON,
    @CCD(label = "Video Conference Hearing")
    @JsonProperty("disposalHearingMethodVideoConferenceHearing")
    DISPOSAL_HEARING_METHOD_VIDEO_CONFERENCE_HEARING,
    @CCD(label = "Telephone Hearing")
    @JsonProperty("disposalHearingMethodTelephoneHearing")
    DISPOSAL_HEARING_METHOD_TELEPHONE_HEARING

}
