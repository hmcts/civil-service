package uk.gov.hmcts.reform.civil.enums.dj;

import lombok.Getter;

@Getter
public enum HearingMethodVideoConferenceDJ {
    videoTheClaimant("the claimant"),
    videoTheDefendant("the defendant"),
    videoTheCourt("the court");

    private String label;

    HearingMethodVideoConferenceDJ(String value) {
        this.label = value;
    }

}

