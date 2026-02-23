package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;

import java.math.BigDecimal;

@Accessors(chain = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
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
