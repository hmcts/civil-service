package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LoanCardDebtLRspec {

    private final String loanCardDebtDetail;
    private final BigDecimal totalOwed;
    private final BigDecimal monthlyPayment;

}
