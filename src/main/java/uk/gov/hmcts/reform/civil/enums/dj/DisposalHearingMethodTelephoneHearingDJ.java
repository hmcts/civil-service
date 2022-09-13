package uk.gov.hmcts.reform.civil.enums.dj;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Getter
public enum DisposalHearingMethodTelephoneHearingDJ {
    telephoneTheClaimant("the claimant"),
    telephoneTheDefendant("the defendant"),
    getTelephoneTheCourt("the cour");

    private String label;

    DisposalHearingMethodTelephoneHearingDJ(String value) { this.label = value;
    }

}

