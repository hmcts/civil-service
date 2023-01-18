package uk.gov.hmcts.reform.civil.enums.dj;

import lombok.Getter;

@Getter
public enum HearingMethodTelephoneHearingDJ {
    telephoneTheClaimant("the claimant"),
    telephoneTheDefendant("the defendant"),
    telephoneTheCourt("the court");

    private String label;

    HearingMethodTelephoneHearingDJ(String value) {
        this.label = value;
    }

}

