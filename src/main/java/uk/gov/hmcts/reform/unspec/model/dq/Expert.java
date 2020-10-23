package uk.gov.hmcts.reform.unspec.model.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Expert {

    private final String name;
    private final String fieldOfExpertise;
    private final String whyRequired;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal estimatedCost;
}
