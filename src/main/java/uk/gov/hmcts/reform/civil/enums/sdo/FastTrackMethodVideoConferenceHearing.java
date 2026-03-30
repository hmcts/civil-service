package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum FastTrackMethodVideoConferenceHearing {
    VIDEO_THE_CLAIMANT("the claimant"),
    VIDEO_THE_DEFENDANT("the defendant"),
    VIDEO_THE_COURT("the court");

    private final String label;

    FastTrackMethodVideoConferenceHearing(String value) {
        this.label = value;
    }
}
