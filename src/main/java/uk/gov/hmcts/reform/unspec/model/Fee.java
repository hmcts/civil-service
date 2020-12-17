package uk.gov.hmcts.reform.unspec.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.unspec.utils.MonetaryConversions;

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
            .calculatedAmount(MonetaryConversions.penniesToPounds(calculatedAmountInPence))
            .code(code)
            .version(version)
            .build();
    }
}
