package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;

import java.math.BigDecimal;

@Data
@Builder
public class Fee {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal calculatedAmountInPence;
    private String code;
    private String version;

    public FeeDto toFeeDto() {
        return FeeDto.builder()
            .calculatedAmount(toPounds())
            .code(code)
            .version(version)
            .build();
    }

    public BigDecimal toPounds() {
        return MonetaryConversions.penniesToPounds(this.calculatedAmountInPence);
    }

    public String formData() {
        return "£" + this.toPounds();
    }
}
