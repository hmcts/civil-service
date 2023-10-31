package uk.gov.hmcts.reform.civil.model.allowance;

import lombok.Getter;

@Getter
public enum PersonalAllowance {

    SINGLE_UNDER_25(250.90),
    SINGLE_OVER_25(316.76),
    COUPLES_UNDER_18(379.16),
    COUPLES_UNDER_18_UNDER_25(250.90),
    COUPLES_UNDER_18_OVER_25(316.76),
    COUPLES_OVER_18(497.68);

    private double allowance;

    PersonalAllowance(double allowance) {
        this.allowance = allowance;
    }

    public static PersonalAllowance getPersonalAllowance(int age, boolean hasPartner, boolean partnerOver18) {
        boolean under25 = 25 > age;
        boolean over25 = 25 <= age;
        if (hasPartner && over25 && partnerOver18) {
            return COUPLES_OVER_18;
        }
        if (hasPartner && over25 && !partnerOver18) {
            return COUPLES_UNDER_18_OVER_25;
        }
        if (hasPartner && under25 && !partnerOver18) {
            return COUPLES_UNDER_18_UNDER_25;
        }
        if (!hasPartner && under25) {
            return SINGLE_UNDER_25;
        }
        if (!hasPartner && over25) {
            return SINGLE_OVER_25;
        }
        return COUPLES_UNDER_18;
    }
}
