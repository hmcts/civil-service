package uk.gov.hmcts.reform.civil.enums.dj;

import lombok.Getter;

@Getter
public enum HearingMethodTelephoneHearingDJ {
    TELEPHONE_THE_CLAIMANT("the claimant"),
    TELEPHONE_THE_DEFENDANT("the defendant"),
    TELEPHONE_THE_COURT("the court");

    private String label;

    HearingMethodTelephoneHearingDJ(String value) {
        this.label = value;
    }

}

