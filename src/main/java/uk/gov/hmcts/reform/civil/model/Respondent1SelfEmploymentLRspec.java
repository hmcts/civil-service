package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
public class Respondent1SelfEmploymentLRspec {

    private String jobTitle;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal annualTurnover;
    private YesOrNo isBehindOnTaxPayment;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amountOwed;
    private String reason;

}
