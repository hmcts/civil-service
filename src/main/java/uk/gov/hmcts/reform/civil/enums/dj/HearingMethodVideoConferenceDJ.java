package uk.gov.hmcts.reform.civil.enums.dj;

import lombok.Getter;

@Getter
public enum HearingMethodVideoConferenceDJ {
    VIDEO_THE_CLAIMANT("the claimant"),
    VIDEO_THE_DEFENDANT("the defendant"),
    VIDEO_THE_COURT("the court");

    private String label;

    HearingMethodVideoConferenceDJ(String value) {
        this.label = value;
    }

}

