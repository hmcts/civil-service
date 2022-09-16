package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum FastTrackMethodTelephoneHearing {
    telephoneTheClaimant("the claimant"),
    telephoneTheDefendant("the defendant"),
    telephoneTheCourt("the court");

    private final String label;

    FastTrackMethodTelephoneHearing(String value) {
        this.label = value;
    }
}
