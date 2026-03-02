package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Respondent1SelfEmploymentLRspec {

    private String jobTitle;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal annualTurnover;
    private YesOrNo isBehindOnTaxPayment;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amountOwed;
    private String reason;

}
