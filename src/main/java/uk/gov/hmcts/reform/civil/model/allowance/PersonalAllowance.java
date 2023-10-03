package uk.gov.hmcts.reform.civil.model.allowance;

import lombok.Getter;

@Getter
public enum PersonalAllowance {

    SINGLE_UNDER_25(250.90),
    SINGLE_OVER_25(316.76),
    COUPLES_UNDER_18(379.16),
    COUPLES_UNDER_18_UNDER_25(250.90),
    COUPLES_UNDER_18_OVER_25(250.90),
    COUPLES_OVER_18(497.68);


    private double allowance;

    PersonalAllowance(double allowance) {
        this.allowance = allowance;
    }
}
