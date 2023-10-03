package uk.gov.hmcts.reform.civil.model.allowance;

import lombok.Getter;

@Getter
public enum DisabilityAllowance {

    SINGLE(139.75),
    COUPLE(199.12),
    SEVERE_DISABILITY_SINGLE(268.01),
    SEVERE_DISABILITY_COUPLE(536.03),
    DISABLED_DEPENDANT(260.26);

    private double allowance;

    DisabilityAllowance(double allowance) {
        this.allowance = allowance;
    }
}
