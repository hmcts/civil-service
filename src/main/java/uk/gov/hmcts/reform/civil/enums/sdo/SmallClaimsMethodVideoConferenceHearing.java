package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum SmallClaimsMethodVideoConferenceHearing {
    videoTheClaimant("the claimant"),
    videoTheDefendant("the defendant"),
    videoTheCourt("the court");

    private final String label;

    SmallClaimsMethodVideoConferenceHearing(String value) {
        this.label = value;
    }
}
