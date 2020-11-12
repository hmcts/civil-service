package uk.gov.hmcts.reform.unspec.model.robotics;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ClaimDetails {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal amountClaimed;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal courtFee;
}
