package uk.gov.hmcts.reform.civil.model.robotics;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ClaimDetails {

    private String caseRequestReceivedDate;
    private String caseIssuedDate;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private BigDecimal amountClaimed;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal totalInterest;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal totalClaimAmountWithInterest;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private BigDecimal courtFee;
}
