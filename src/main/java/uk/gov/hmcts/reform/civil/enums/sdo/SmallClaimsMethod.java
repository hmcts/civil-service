package uk.gov.hmcts.reform.civil.enums.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SmallClaimsMethod {
    @JsonProperty("smallClaimsMethodInPerson")
    SMALL_CLAIMS_METHOD_IN_PERSON,
    @JsonProperty("smallClaimsMethodVideoConferenceHearing")
    SMALL_CLAIMS_METHOD_VIDEO_CONFERENCE_HEARING,
    @JsonProperty("smallClaimsMethodTelephoneHearing")
    SMALL_CLAIMS_METHOD_TELEPHONE_HEARING
}
