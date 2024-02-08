package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Getter;

@Getter
public enum SmallClaimsSdoR2PhysicalTrialBundleOptions {
    NONE("None"),
    PARTY("Party");

    private final String label;

    SmallClaimsSdoR2PhysicalTrialBundleOptions(String value) {
        this.label = value;
    }
}
