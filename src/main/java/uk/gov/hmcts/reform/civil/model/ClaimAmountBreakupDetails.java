package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ClaimAmountBreakupDetails {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal claimAmount;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String claimReason;

    @JsonCreator
    public ClaimAmountBreakupDetails(@JsonProperty("claimAmount") BigDecimal claimAmount,
                                     @JsonProperty("claimReason") String claimReason) {
        this.claimAmount = claimAmount;
        this.claimReason = claimReason;
    }

}