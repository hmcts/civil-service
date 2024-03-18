package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum SdoR2FastTrackMethod {

    fastTrackMethodVideoConferenceHearing("Video"),
    fastTrackMethodTelephoneHearing("Telephone"),
    fastTrackMethodInPerson("In person");

    private final String label;

    SdoR2FastTrackMethod(String value) {
        this.label = value;
    }

}
