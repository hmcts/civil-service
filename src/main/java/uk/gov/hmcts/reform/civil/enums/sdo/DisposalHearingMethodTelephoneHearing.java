package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum DisposalHearingMethodTelephoneHearing {
    telephoneTheClaimant("the claimant"),
    telephoneTheDefendant("the defendant"),
    telephoneTheCourt("the court");

    private final String label;

    DisposalHearingMethodTelephoneHearing(String value) {
        this.label = value;
    }
}
