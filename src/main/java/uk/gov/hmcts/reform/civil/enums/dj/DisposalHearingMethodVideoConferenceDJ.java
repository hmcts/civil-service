package uk.gov.hmcts.reform.civil.enums.dj;

import lombok.Getter;

@Getter
public enum DisposalHearingMethodVideoConferenceDJ {
    videoTheClaimant("the claimant"),
    videoTheDefendant("the defendant"),
    videoTheCourt("the court");

    private String label;

    DisposalHearingMethodVideoConferenceDJ(String value) { this.label = value;
    }

}

