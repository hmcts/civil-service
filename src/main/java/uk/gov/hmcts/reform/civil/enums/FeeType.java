package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;

@Getter
public enum FeeType {

    CLAIMISSUED("claim", "hawlio"),
    HEARING("hearing", "clyw");

    private final String label;
    private final String labelInWelsh;

    FeeType(String label, String labelInWelsh) {
        this.label = label;
        this.labelInWelsh = labelInWelsh;
    }
}
