package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class RepaymentPlanLRspec {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal paymentAmount;
    private final String repaymentFrequency;
    private final double repaymentLength;
    private final LocalDate firstRepaymentDate;



}
