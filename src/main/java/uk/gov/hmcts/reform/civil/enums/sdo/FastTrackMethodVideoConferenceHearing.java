package uk.gov.hmcts.reform.civil.enums.sdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public enum FastTrackMethodVideoConferenceHearing {
    @JsonProperty("videoTheClaimant")
    VIDEO_THE_CLAIMANT("the claimant"),
    @JsonProperty("videoTheDefendant")
    VIDEO_THE_DEFENDANT("the defendant"),
    @JsonProperty("videoTheCourt")
    VIDEO_THE_COURT("the court");

    private final String label;

    FastTrackMethodVideoConferenceHearing(String value) {
        this.label = value;
    }
}
