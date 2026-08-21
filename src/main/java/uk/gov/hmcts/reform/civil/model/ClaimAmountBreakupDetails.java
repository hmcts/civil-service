package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ClaimAmountBreakup", generate = true)
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ClaimAmountBreakupDetails {

    @CCD(label = "Amount", searchable = false, typeOverride = FieldType.MoneyGBP)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal claimAmount;
    @CCD(
            label = "What you are claiming for",
            hint = "Briefly explain each item, for example: broken tiles, roof damage.",
            searchable = false
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String claimReason;

    @JsonCreator
    public ClaimAmountBreakupDetails(@JsonProperty("claimAmount") BigDecimal claimAmount,
                                     @JsonProperty("claimReason") String claimReason) {
        this.claimAmount = claimAmount;
        this.claimReason = claimReason;
    }

}