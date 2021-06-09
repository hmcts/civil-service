package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ClaimAmountBreakupDetails {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal claimAmount;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final String claimReason;

    @JsonCreator
    public ClaimAmountBreakupDetails(@JsonProperty("claimAmount") BigDecimal claimAmount,
                                     @JsonProperty("claimReason") String claimReason) {
        this.claimAmount = claimAmount;
        this.claimReason = claimReason;
    }

}
