package uk.gov.hmcts.reform.civil.enums.dj;

import lombok.Getter;

@Getter
public enum DisposalHearingMethodTelephoneHearingDJ {
    telephoneTheClaimant("the claimant"),
    telephoneTheDefendant("the defendant"),
    telephoneTheCourt("the court");

    private String label;

    DisposalHearingMethodTelephoneHearingDJ(String value) { this.label = value;
    }

}

