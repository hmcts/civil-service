package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum DisposalHearingMethodVideoConferenceHearing {
    videoTheClaimant("the claimant"),
    videoTheDefendant("the defendant"),
    videoTheCourt("the court");

    private final String label;

    DisposalHearingMethodVideoConferenceHearing(String value) {
        this.label = value;
    }
}
