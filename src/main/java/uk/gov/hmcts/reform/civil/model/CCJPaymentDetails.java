package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CCJPaymentDetails {

    private YesOrNo ccjPaymentPaidSomeOption;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjJudgmentAmountClaimAmount;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjPaymentPaidSomeAmount;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjJudgmentAmountClaimFee;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjPaymentPaidSomeAmountInPounds;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjJudgmentSummarySubtotalAmount;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjJudgmentTotalStillOwed;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjJudgmentAmountInterestToDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjJudgmentFixedCostAmount;
    private YesOrNo ccjJudgmentFixedCostOption;
    private String ccjJudgmentStatement;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjJudgmentLipInterest;

}