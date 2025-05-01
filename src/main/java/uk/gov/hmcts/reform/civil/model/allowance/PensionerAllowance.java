package uk.gov.hmcts.reform.civil.model.allowance;

import lombok.Getter;

@Getter
public enum PensionerAllowance {

    SINGLE(335.83),
    COUPLE(502.66);

    private double allowance;

    PensionerAllowance(double allowance) {
        this.allowance = allowance;
    }

    public static double getPensionerAllowance(boolean pensioner, boolean partnerPensioner) {
        if ((pensioner && !partnerPensioner) || (!pensioner && partnerPensioner)) {
            return SINGLE.allowance;
        }
        if (pensioner) {
            return COUPLE.allowance;
        }
        return 0.0;
    }
}
