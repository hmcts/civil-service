package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PayingMoneyDetails {

    private String claimNumberText;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amountOwed;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal monthlyInstalmentAmount;

}
