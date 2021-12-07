package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LoanCardDebtLRspec {

    private final String loanCardDebtDetail;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal totalOwed;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal monthlyPayment;

}
