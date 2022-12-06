package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum SmallClaimsMethodTelephoneHearing {
    telephoneTheClaimant("the claimant"),
    telephoneTheDefendant("the defendant"),
    telephoneTheCourt("the court");

    private final String label;

    SmallClaimsMethodTelephoneHearing(String value) {
        this.label = value;
    }
}
