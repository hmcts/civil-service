package uk.gov.hmcts.reform.civil.model.citizenui.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public class RepaymentPlanDecisionDto {

    private LocalDate repaymentDate;
    private RepaymentDecisionType repaymentDecisionType;
    private double repaymentInstallmentAmount;
}
