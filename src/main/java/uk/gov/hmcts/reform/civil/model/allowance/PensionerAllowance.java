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
}
