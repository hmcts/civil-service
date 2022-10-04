package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum FastTrackMethodVideoConferenceHearing {
    videoTheClaimant("the claimant"),
    videoTheDefendant("the defendant"),
    videoTheCourt("the court");

    private final String label;

    FastTrackMethodVideoConferenceHearing(String value) {
        this.label = value;
    }
}
