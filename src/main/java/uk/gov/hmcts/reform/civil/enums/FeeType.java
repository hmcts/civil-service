package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;

@Getter
public enum FeeType {
    CLAIMISSUED("claim", "hawliad"),
    HEARING("hearing", "gwrandawiad"),
    APPLICATION("application", "cais"),
    ADDITIONAL("additional", "cais ychwanegol");

    private final String label;
    private final String labelInWelsh;

    FeeType(String label, String labelInWelsh) {
        this.label = label;
        this.labelInWelsh = labelInWelsh;
    }
}
