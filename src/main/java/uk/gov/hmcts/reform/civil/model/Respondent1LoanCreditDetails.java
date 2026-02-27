package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Respondent1LoanCreditDetails {

    private String loanCardDebtDetail;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal totalOwed;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal monthlyPayment;

}
