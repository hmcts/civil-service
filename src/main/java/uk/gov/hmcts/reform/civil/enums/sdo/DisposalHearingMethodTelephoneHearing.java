package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum DisposalHearingMethodTelephoneHearing {
    TELEPHONE_THE_CLAIMANT("the claimant"),
    TELEPHONE_THE_DEFENDANT("the defendant"),
    TELEPHONE_THE_COURT("the court");

    private final String label;

    DisposalHearingMethodTelephoneHearing(String value) {
        this.label = value;
    }
}
