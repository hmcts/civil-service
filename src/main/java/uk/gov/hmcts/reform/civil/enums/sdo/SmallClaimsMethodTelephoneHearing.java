package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum SmallClaimsMethodTelephoneHearing {
    TELEPHONE_THE_CLAIMANT("the claimant"),
    TELEPHONE_THE_DEFENDANT("the defendant"),
    TELEPHONE_THE_COURT("the court");

    private final String label;

    SmallClaimsMethodTelephoneHearing(String value) {
        this.label = value;
    }
}
