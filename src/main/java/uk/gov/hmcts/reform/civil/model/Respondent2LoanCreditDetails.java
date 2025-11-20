package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class Respondent2LoanCreditDetails {

    private String loanCardDebtDetail;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalOwed;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal monthlyPayment;

}
